package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import de.focus_shift.jollyday.core.HolidayManager;
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
public class ManagerOpeningHourAnalysisStrategy implements ScheduleAnalysisStrategy{
    private final HolidayManager holidayManager;

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.MANAGER_OPENING_HOUR;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
        Map<LocalDate, int[]> originalStoreDraft = context.getUneditedOriginalDateStoreDraft();

        int targetHour =  findStoreOpenHour(originalStoreDraft,day);

        int targetHourDemandDraftValue = originalStoreDraft.getOrDefault(day,new int[24])[targetHour];

        int[] arrayDailyProposalsCount = new int[24];
        int proposalEmployeesCanOpenCloseStoreCount = 0;
        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day,new HashMap<>());
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
        log.info("");
        log.info("managerOpeningHourAnalysisStrategy");
        List<Shift> shifts = ((ManagerOpeningHourAnalysisResult) result).shifts();

        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().get(day);

        Optional<Employee> employeeWithHighestMonthlyWorkingHours = ((ManagerOpeningHourAnalysisResult) result).employeesWithOpenStoreProposals().stream()
                .max(Comparator.comparingInt(empl -> context.getEmployeeHours().getOrDefault(empl,0)));

        if (employeeWithHighestMonthlyWorkingHours.isEmpty()){
            if (!holidayManager.isHoliday(day)) {
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
            }
            return;
        }

        Optional<Shift> shiftToChangeStartHour = shifts.stream()
                .min(longestOpenStoreShift());

        if (shiftToChangeStartHour.isEmpty()){
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
        Shift shiftToChange = shiftToChangeStartHour.get();

        Shift changedProposalShift = context.findShiftByHours(shiftToChange.getStartHour(), originalProposalShift.getEndHour());

        changeProposalShiftInSchedule(day,chosenEmployee,context,originalProposalShift,changedProposalShift);
        context.updateEmployeeDailyProposal(chosenEmployee,day, context.shiftAsArray(changedProposalShift));

        shiftToChangeStartHour.get().setStartHour(originalProposalShift.getStartHour());
        log.info("");
    }

    private int findStoreOpenHour(Map<LocalDate, int[]> originalStoreDrafts, LocalDate day) {
        int targetHour = 0;

        for (int i = 0; i < 24; i++){
            if (originalStoreDrafts.getOrDefault(day,new int[24])[i] > 0){
                targetHour = i;
                break;
            }
        }
        return targetHour;
    }

    private void changeProposalShiftInSchedule(LocalDate date, Employee employee, ScheduleGeneratorContext context, Shift oldShift, Shift newShift) {
        context.updateShiftOnSchedule(date,employee,newShift);
        context.updateEmployeeDailyProposal(employee,date,context.shiftAsArray(newShift));
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