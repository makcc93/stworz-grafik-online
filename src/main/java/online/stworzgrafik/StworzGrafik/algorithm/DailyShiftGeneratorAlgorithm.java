package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyShiftGeneratorAlgorithm {

    public void generateShiftsToDays(ScheduleGeneratorContext context) {
        Map<LocalDate, int[]> everyDayStoreDemandDraft = context.getUneditedOriginalDateStoreDraft();
        List<Employee> employees = context.getStoreActiveEmployees();

        for (Map.Entry<LocalDate, int[]> entry : everyDayStoreDemandDraft.entrySet()) {
            int[] employeeDailyProposalCount = new int[24];
            LocalDate date = entry.getKey();


            int[] dailyDraft = entry.getValue();
            Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate = context.getMonthlyEmployeesProposalShiftsByDate();
            Map<Employee, int[]> dailyEmployeeProposals = monthlyEmployeesProposalShiftsByDate.getOrDefault(date, Collections.emptyMap());

            for (Employee employee : employees) {
                int[] employeeProposal = dailyEmployeeProposals.getOrDefault(employee, new int[24]);

                employeeDailyProposalCount = addArrays(employeeDailyProposalCount,employeeProposal);
            }

            int[] draftAfterProposals = subtractArrays(dailyDraft, employeeDailyProposalCount);
            List<Shift> shifts = generateLowestPersonNeededDailyShifts(draftAfterProposals);

            context.addShiftsToDay(date,shifts);
        }
    }

    private List<Shift> generateLowestPersonNeededDailyShifts(int[] dailyDemandDraft) {
        List<Shift> startHoursShifts = generateShiftStartHours(dailyDemandDraft);

        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
                .toList();

        return generateShiftEndHours(shiftsSortedDesc, dailyDemandDraft);
    }

    private List<Shift> generateShiftEndHours(List<Shift> shiftsSortedDesc, int[] dailyDemandDraft) {
        int index = 0;
        for (int hourOfDay = 23; hourOfDay >= 0; hourOfDay--) {
            int demand = dailyDemandDraft[hourOfDay];

            if (demand > 0) {
                int nextDemand = (hourOfDay == 23) ? 0 : dailyDemandDraft[hourOfDay + 1];
                for (int i = demand; i > nextDemand; i--) {

                    if (hourOfDay == 23){
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(0,0));
                    } else {
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(hourOfDay + 1, 0));
                    }
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
                int previousDemand = (hourOfDay == 0) ? 0 : dailyDemandDraft[hourOfDay -1];
                for (int i = demand; i > previousDemand; i--) {
                    Shift shift = new Shift();
                    shift.setStartHour(LocalTime.of(hourOfDay,0));

                    shifts.add(shift);
                }
            }
        }
        return shifts;
    }

    private int[] addArrays(int[] mainArray, int[] addedArray){
        int[] result = new int[24];
        for (int i = 0; i < 24; i++){
            result[i] = mainArray[i] + addedArray[i];
        }

        return result;
    }

    private int[] subtractArrays(int[] storeDraft, int[] proposalsSum){
        int[] result = new int[24];
        for (int i = 0; i < 24; i++){
            result[i] = Math.max(0,storeDraft[i] - proposalsSum[i]);
        }

        return result;
    }
}

