package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

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
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.MANAGER_CLOSING_HOUR;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
        Map<LocalDate, int[]> originalStoreDraft = context.getUneditedOriginalDateStoreDraft();

        int targetHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(day).closeHour();

        int targetHourDemandDraftValue = originalStoreDraft.getOrDefault(day, new int[24])[targetHour];

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

        if (employeeWithHighestMonthlyWorkingHours.isEmpty()) {
            log.info("Brak dostępnego pracownika w dniu {},", day);

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

        Optional<Shift> shiftToChangeEndHour = shifts.stream()
                .min(longestCloseStoreShift());

        if (shiftToChangeEndHour.isEmpty()){
            log.info("Brak dostępnej zmiany w dniu {}", day);

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej zmiany w dniu: " + day,
                            null,
                            day
                    )
            );
            return;
        }

        Employee chosenEmployee = employeeWithHighestMonthlyWorkingHours.get();

        Shift originalProposalShift = context.findShiftByArray(dailyProposals.getOrDefault(chosenEmployee, new int[24]));
        log.info("originalProposalShift: {}-{}", originalProposalShift.getStartHour(), originalProposalShift.getEndHour());

        Shift shiftToChange = shiftToChangeEndHour.get();
        log.info("shiftToChange z shiftsSorted: {}-{}", shiftToChange.getStartHour(),shiftToChange.getEndHour());

        Shift changedProposalShift = context.findShiftByHours(originalProposalShift.getStartHour(),shiftToChange.getEndHour());
        changeProposalShiftInSchedule(day,chosenEmployee,context,originalProposalShift,changedProposalShift);
        context.updateEmployeeDailyProposal(chosenEmployee,day, context.shiftAsArray(changedProposalShift));

        shiftToChangeEndHour.get().setEndHour(originalProposalShift.getEndHour());
    }

    private void changeProposalShiftInSchedule(LocalDate date, Employee employee, ScheduleGeneratorContext context, Shift oldShift, Shift newShift) {
        context.updateShiftOnSchedule(date,employee,newShift);
        context.updateEmployeeDailyProposal(employee,date,context.shiftAsArray(newShift));
    }

    private Comparator<Shift> longestCloseStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getEndHour().getHour()
                ).reversed()
                .thenComparing(
                        Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ).reversed()
                );
    }
}
