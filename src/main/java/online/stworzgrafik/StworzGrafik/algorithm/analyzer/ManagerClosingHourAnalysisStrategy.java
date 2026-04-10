package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerClosingHourAnalysisStrategy implements ScheduleAnalysisStrategy{

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

        Optional<Employee> employeeWithHighestMonthlyWorkingHours = ((ManagerClosingHourAnalysisResult) result).employeesWithCloseStoreProposals().stream()
                .max(Comparator.comparingInt(empl -> context.getEmployeeHours().getOrDefault(empl, 0)));

        if (employeeWithHighestMonthlyWorkingHours.isEmpty()){
            log.info("Brak dostępnego pracownika w dniu {},",day);

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak dostępnego pracownika w dniu: " + day,
                            null,
                            day
                    )
            );
            return;
        }

        Employee chosenEmployee = employeeWithHighestMonthlyWorkingHours.get();

        Shift originalProposalShift = context.findShiftByArray(dailyProposals.getOrDefault(chosenEmployee, new int[24]));
        Shift endHourDecrementShift = context.findShiftByHours(originalProposalShift.getStartHour(),originalProposalShift.getEndHour().minusHours(1));

        changeProposalShiftInSchedule(day,chosenEmployee,context,originalProposalShift,endHourDecrementShift);

        context.updateEmployeeDailyProposal(chosenEmployee,day, context.shiftAsArray(endHourDecrementShift));

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

    private void changeProposalShiftInSchedule(LocalDate date, Employee employee, ScheduleGeneratorContext context, Shift oldShift, Shift newShift) {
        context.updateShiftOnSchedule(date,employee,newShift);
        context.updateEmployeeDailyProposal(employee,date,context.shiftAsArray(newShift));
        context.updateEmployeeHours(employee,oldShift,newShift);
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
