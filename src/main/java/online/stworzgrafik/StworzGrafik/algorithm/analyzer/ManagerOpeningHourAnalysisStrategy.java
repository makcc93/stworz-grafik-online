package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerOpeningHourAnalysisStrategy implements ScheduleAnalysisStrategy{
    private final ScheduleMessageService scheduleMessageService;
    private final ShiftEntityService shiftEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.MANAGER_OPENING_HOUR;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
        Map<LocalDate, int[]> originalStoreDraft = context.getUneditedOriginalDateStoreDraft();

        int targetHour =  findOpenStoreHour(originalStoreDraft,day);

        int targetHourDemandDraftValue = originalStoreDraft.get(day)[targetHour];

        int[] arrayDailyProposalsCount = new int[24];
        int proposalEmployeesCanOpenCloseStoreCount = 0;
        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        List<Employee> employeesWithOpenCloseStoreProposals = new ArrayList<>();

        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposals.entrySet()){
            Employee employee = proposalEntry.getKey();

            if (employee.isCanOpenCloseStore()){
                proposalEmployeesCanOpenCloseStoreCount++;
            }

            int[] singleEmployeeProposal = proposalEntry.getValue();
            for (int i = 0; i < arrayDailyProposalsCount.length; i++){
                arrayDailyProposalsCount[i] += singleEmployeeProposal[i];
            }


            if (singleEmployeeProposal[targetHour] > 0){
                employeesWithOpenCloseStoreProposals.add(employee);
            }
        }

        int targetHourProposalsCount = arrayDailyProposalsCount[targetHour];

        return new ManagerOpeningHourAnalysisResult(targetHour,targetHourDemandDraftValue,targetHourProposalsCount,proposalEmployeesCanOpenCloseStoreCount,employeesWithOpenCloseStoreProposals,shifts);

    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((ManagerOpeningHourAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        List<Shift> shifts = ((ManagerOpeningHourAnalysisResult) result).shifts();

        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().get(day);

        Optional<Employee> employeeWithHighestMonthlyWorkingHours = ((ManagerOpeningHourAnalysisResult) result).employeesWithOpenStoreProposals().stream()
                .sorted(Comparator.comparingInt(empl -> context.getEmployeeHours().get(empl))
                        .reversed())
                .findFirst();

        if (employeeWithHighestMonthlyWorkingHours.isEmpty()){
            log.info("Brak dostępnego pracownika w dniu {},",day);

            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak dostępnego pracownika w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        Shift originalProposalShift = shiftEntityService.getArrayAsShift(dailyProposals.get(employeeWithHighestMonthlyWorkingHours.get()));
        Shift startHourIncrementShift = shiftEntityService.getEntityByHours(originalProposalShift.getStartHour().plusHours(1), originalProposalShift.getEndHour());

        ScheduleDetails employeeOldShiftInSchedule = scheduleDetailsEntityService.findEmployeeScheduleDetailsByDay(context.getStoreId(), context.getSchedule().getId(), employeeWithHighestMonthlyWorkingHours.get(), day);

        changeProposalShiftInSchedule(context, employeeOldShiftInSchedule, startHourIncrementShift);

        context.updateEmployeeDailyProposal(employeeWithHighestMonthlyWorkingHours.get(),day, shiftEntityService.getShiftAsArray(startHourIncrementShift));

        updateShiftsInMatcher(context, shifts, day);
    }

    private int findOpenStoreHour(Map<LocalDate, int[]> originalStoreDrafts, LocalDate day) {
        int targetHour = 0;
        for (int i = 0; i < originalStoreDrafts.get(day).length; i++){
            if (originalStoreDrafts.get(day)[i] > 0){
                targetHour = i;
                break;
            }
        }
        return targetHour;
    }

    private void updateShiftsInMatcher(ScheduleGeneratorContext context, List<Shift> shifts, LocalDate day) {
        Optional<Shift> shiftToChangeStartHour = shifts.stream()
                .sorted(longestOpenStoreShift())
                .findFirst();

        if (shiftToChangeStartHour.isEmpty()){
            log.info("Brak dostępnej zmiany w dniu {}", day);

            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej zmiany w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        shiftToChangeStartHour.get().setStartHour(shiftToChangeStartHour.get().getStartHour().minusHours(1));
    }

    private void changeProposalShiftInSchedule(ScheduleGeneratorContext context, ScheduleDetails employeeOldShiftInSchedule, Shift startHourIncrementShift) {
        scheduleDetailsEntityService.updateEntityScheduleDetails(context.getStoreId(), context.getSchedule().getId(), employeeOldShiftInSchedule.getId(),
                new UpdateScheduleDetailsDTO(
                        null,
                        null,
                        startHourIncrementShift.getId(),
                        null
                )
        );

        context.updateEmployeeHours(employeeOldShiftInSchedule.getEmployee(),employeeOldShiftInSchedule.getShift(),startHourIncrementShift);
    }

    private static Comparator<Shift> longestOpenStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getStartHour().getHour()
                )
                .thenComparing(
                        Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ).reversed()
                );
    }
}