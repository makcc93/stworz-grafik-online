package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TooManyProposalsAnalysisStrategy implements ScheduleAnalysisStrategy{
    private final ScheduleMessageService scheduleMessageService;
    private final ShiftEntityService shiftEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final ScheduleDetailsService scheduleDetailsService;

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.TOO_MANY_PROPOSALS;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
        Map<Employee, int[]> employeeDailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day,Collections.emptyMap());
        int[] proposalsCount = getEmployeesDailyProposalCount(employeeDailyProposals);

        int[] originalDailyDraft = context.getUneditedOriginalDateStoreDraft().get(day);

        return new TooManyProposalsAnalysisResult(originalDailyDraft,proposalsCount);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((TooManyProposalsAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        int[] originalDailyDraft = ((TooManyProposalsAnalysisResult) result).originalDailyDraft();
        int[] proposalsCount = ((TooManyProposalsAnalysisResult) result).proposalsCount();

        for (int indexHour = 0; indexHour < originalDailyDraft.length; indexHour++){
            while (originalDailyDraft[indexHour] < proposalsCount[indexHour]){
                boolean resolved = adaptProposalHourToDemandDraft(context, day, indexHour, proposalsCount);
                if (!resolved) break;
            }
        }
    }

    private boolean adaptProposalHourToDemandDraft(ScheduleGeneratorContext context, LocalDate day, int indexHour, int[] proposalsCount){
        Map<Employee, int[]> employeesDailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, Collections.emptyMap());

        Optional<Employee> employeeWithHighestWorkingHoursCannotOpenStore = context.getStoreActiveEmployees().stream()
                .filter(empl -> !empl.isCanOpenCloseStore())
                .filter(empl -> {
                            int[] proposal = context.getMonthlyEmployeesProposalShiftsByDate()
                                    .getOrDefault(day, Collections.emptyMap())
                                    .get(empl);

                            return proposal != null && proposal[indexHour] > 0;
                        }
                )
                .sorted((empl1, empl2) -> context.getEmployeeHours().getOrDefault(empl2, 0).compareTo(context.getEmployeeHours().getOrDefault(empl1, 0)))
                .findFirst();

        if (employeeWithHighestWorkingHoursCannotOpenStore.isEmpty()){
            scheduleMessageService.addMessage(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z największą liczbą przepracowanych godzin, który nie może otwierać sklepu",
                            null,
                            day
                    )
            );
            return false;
        }

        int[] adaptedProposal = modifyOriginalProposal(indexHour, employeesDailyProposals, employeeWithHighestWorkingHoursCannotOpenStore.get());

        context.updateEmployeeDailyProposal(employeeWithHighestWorkingHoursCannotOpenStore.get(),day,adaptedProposal);
        reduceProposalCount(proposalsCount,indexHour);
        
        return true;
    }

    private static int[] modifyOriginalProposal(int indexHour, Map<Employee, int[]> employeesDailyProposals, Employee employeeWithHighestWorkingHoursCannotOpenStore) {
        int[] dailyProposal = employeesDailyProposals.getOrDefault(employeeWithHighestWorkingHoursCannotOpenStore, new int[24]);
        int[] adaptedProposal = new int[24];

        for (int i = 0; i < dailyProposal.length; i++){
            if (i == indexHour){
                adaptedProposal[i] = 0;
                continue;
            }
            
            adaptedProposal[i] = dailyProposal[i];
        }
        return adaptedProposal;
    }

    private void reduceProposalCount(int[] proposalsCount, int indexHour){
        for (int i = 0; i < proposalsCount.length; i++){
            if (i == indexHour){
                proposalsCount[i] -= 1;
            }
        }
    }

    private int[] getEmployeesDailyProposalCount(Map<Employee, int[]> employeeDailyProposals){
        int[] proposalsCount = new int[24];
        for (Employee employee : employeeDailyProposals.keySet()){
            int[] employeeProposal = employeeDailyProposals.get(employee);

            for (int i = 0; i < employeeProposal.length; i++){
                proposalsCount[i] += employeeProposal[i];
            }
        }

        return proposalsCount;
    }
}
