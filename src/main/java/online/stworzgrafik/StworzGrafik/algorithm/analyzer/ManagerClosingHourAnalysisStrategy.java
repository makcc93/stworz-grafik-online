package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagerClosingHourAnalysisStrategy implements ScheduleAnalysisStrategy{
    private final ScheduleMessageService scheduleMessageService;
    private final ShiftEntityService shiftEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.MANAGER_CLOSING_HOUR;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
        Map<LocalDate, int[]> originalStoreDraft = context.getUneditedOriginalDateStoreDraft();

        int targetHour = findCloseStoreHour(originalStoreDraft,day);

        int targetHourDemandDraftValue = originalStoreDraft.get(day)[targetHour];

        int[] arrayDailyProposalsCount = new int[24];
        int proposalEmployeesCanOpenCloseStoreCount = 0;
        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, Collections.emptyMap());
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

        return new ManagerClosingHourAnalysisResult(targetHour,targetHourDemandDraftValue,targetHourProposalsCount,proposalEmployeesCanOpenCloseStoreCount,employeesWithOpenCloseStoreProposals,shifts);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((ManagerClosingHourAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        List<Shift> shifts = ((ManagerClosingHourAnalysisResult) result).shifts();

        Employee employeeWithHighestMonthlyWorkingHours = ((ManagerClosingHourAnalysisResult) result).employeesWithCloseStoreProposals().stream()
                .sorted(Comparator.comparingInt(empl -> context.getEmployeeHours().get(empl))
                        .reversed())
                .toList()
                .getFirst();

        Shift originalProposalShift = shiftEntityService.getArrayAsShift(dailyProposals.get(employeeWithHighestMonthlyWorkingHours));
        Shift endHourDecrementShift = shiftEntityService.getEntityByHours(originalProposalShift.getStartHour(),originalProposalShift.getEndHour().minusHours(1));

        ScheduleDetails employeeOldShiftInSchedule = scheduleDetailsEntityService.findEmployeeShiftByDay(context.getStoreId(), context.getSchedule().getId(), employeeWithHighestMonthlyWorkingHours, day);

        changeProposalShiftInSchedule(context,employeeOldShiftInSchedule,endHourDecrementShift);

        context.updateEmployeeDailyProposal(employeeWithHighestMonthlyWorkingHours,day, shiftEntityService.getShiftAsArray(endHourDecrementShift));

        updateShiftsInMatcher(shifts);
    }

    private int findCloseStoreHour(Map<LocalDate, int[]> originalStoreDrafts, LocalDate day) {
        int targetHour = 23;

        for (int i = 23; i >= 0; i--){
            if (originalStoreDrafts.get(day)[i] > 0){
                targetHour = i;
                break;
            }
        }
        return targetHour;
    }
    private void updateShiftsInMatcher(List<Shift> shifts) {
        Shift shiftToChangeEndHour = shifts.stream()
                .sorted(longestCloseStoreShift())
                .toList()
                .getFirst();

        shiftToChangeEndHour.setEndHour(shiftToChangeEndHour.getEndHour().plusHours(1));
    }

    private void changeProposalShiftInSchedule(ScheduleGeneratorContext context, ScheduleDetails employeeOldShiftInSchedule, Shift endHourDecrementShift) {
        scheduleDetailsEntityService.updateEntityScheduleDetails(context.getStoreId(), context.getSchedule().getId(), employeeOldShiftInSchedule.getId(),
                new UpdateScheduleDetailsDTO(
                        null,
                        null,
                        endHourDecrementShift.getId(),
                        null
                )
        );

        context.updateEmployeeHours(employeeOldShiftInSchedule.getEmployee(),employeeOldShiftInSchedule.getShift(),endHourDecrementShift);
    }

    private Comparator<Shift> longestCloseStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getEndHour().getHour()
                )
                .thenComparing(
                        Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ).reversed()
                );
    }
}
