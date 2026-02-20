package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
@Service
@RequiredArgsConstructor
public class DailyShiftGeneratorAlgorithm {
    private final ShiftService shiftService;
    private final ShiftEntityService shiftEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final ScheduleEntityService scheduleEntityService;
    private final DemandDraftEntityService dailyDemand;

//    final int[] dailyStoreDemandDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 8, 8, 9, 9, 8, 8, 9, 9, 9, 9, 5, 0, 0, 0}; //then demandDraftGet

    public List<Shift> getDailyStoreShifts(Long storeId, LocalDate date, Pageable pageable){

        // 1. find schedule for this storeId and date
        Schedule schedule = getSchedule(storeId, date, pageable);

        // 2. get employee proposal shifts as array
        List<ScheduleDetails> scheduleDetails = getScheduleDetails(storeId, date, pageable, schedule);
        List<int[]> employeeProposals = new ArrayList<>();

        for (ScheduleDetails details : scheduleDetails){
            employeeProposals.add(shiftService.getShiftAsArray(
                    new ShiftHoursDTO(
                            details.getShift().getStartHour(),
                            details.getShift().getEndHour())
                    )
            );
        }

        // 3. sum employee proposal shifts as array
        int[] proposalsSum = new int[24];

        for (int i = 0; i < 24; i++){
            for (int[] j : employeeProposals){
                proposalsSum[i] = proposalsSum[i] + j[i];
            }
        }

        // 4. get store demand draft for this day
        Page<DemandDraft> dailyPageDraft = dailyDemand.findEntityFilteredDrafts(storeId, date, date, pageable);
        int[] dailyDemand = dailyPageDraft.getContent().getFirst().getHourlyDemand();

        // 5. subtract employee proposal shifts from store demand draft for this day
        int[] finalDraftToCreateShifts = subtractArrays(dailyDemand, proposalsSum);

        // 6. generate shifts from array 4.
        return generateLowestPersonNeededDailyShifts(finalDraftToCreateShifts);
    }

    private int[] subtractArrays(int[] storeDraft, int[] proposalsSum){
        int[] result = new int[24];
        for (int i = 0; i < 24; i++){
            result[i] = storeDraft[i] - proposalsSum[i];
        }

        return result;
    }

    private List<ScheduleDetails> getScheduleDetails(Long storeId, LocalDate date, Pageable pageable, Schedule schedule) {
        ScheduleDetailsSpecificationDTO scheduleDetailsSpecificationDTO = new ScheduleDetailsSpecificationDTO(null, null, date, null, null);
        Page<ScheduleDetails> scheduleDetailsPageByCriteria = scheduleDetailsEntityService.findEntityByCriteria(storeId, schedule.getId(), scheduleDetailsSpecificationDTO, pageable);
        return scheduleDetailsPageByCriteria.getContent();
    }

    private Schedule getSchedule(Long storeId, LocalDate date, Pageable pageable) {
        ScheduleSpecificationDTO scheduleSpecificationDTO = new ScheduleSpecificationDTO(
                null,
                date.getYear(),
                date.getMonthValue(),
                null, null, null, null, null, null);

        Page<Schedule> schedulePageByCriteria = scheduleEntityService.findEntityByCriteria(storeId, scheduleSpecificationDTO, pageable);
        return schedulePageByCriteria.getContent().getFirst();
    }

    private List<Shift> generateLowestPersonNeededDailyShifts(int[] dailyDemandDraft) {
        List<Shift> startHoursShifts = generateShiftStartHours(dailyDemandDraft);

        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
                .toList();

        List<Shift> startEndHoursShifts = generateShiftEndHours(shiftsSortedDesc, dailyDemandDraft);

        //save only for tests then serviceFind
        for (Shift shift : startEndHoursShifts) {
            shiftEntityService.saveEntity(shift);
        }

        return startEndHoursShifts;
    }

    private List<Shift> generateEmployeesProposalShifts(){
        return null;
    }

    private List<Shift> generateShiftEndHours(List<Shift> shiftsSortedDesc, int[] dailyDemandDraft) {
        int index = 0;
        for (int hourOfDay = 23; hourOfDay >= 0; hourOfDay--) {
            int demand = dailyDemandDraft[hourOfDay];

            if (demand > 0) {
                for (int i = demand; i > dailyDemandDraft[hourOfDay + 1]; i--) {
                    if (dailyDemandDraft[hourOfDay+1] < demand){
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(hourOfDay+1,0));
                        index++;

                        continue;
                    }

                    shiftsSortedDesc.get(index).setEndHour(LocalTime.of(hourOfDay,0));
                    index++;
                }
            }
        }
        return shiftsSortedDesc;
    }

    private List<Shift> generateShiftStartHours(int[] dailyDemandDraft) {
        List<Shift> shifts = new ArrayList<>();

            for (int hourOfDay = 0; hourOfDay < dailyDemandDraft.length; hourOfDay++) {
                int demand = dailyDemandDraft[hourOfDay];

                if (demand != 0) {
                    for (int i = demand; i > dailyDemandDraft[hourOfDay - 1]; i--) {
                        Shift shift = new Shift();
                        shift.setStartHour(LocalTime.of(hourOfDay,0));

                        shifts.add(shift);
                    }
                }
            }
            return shifts;
    }
}

