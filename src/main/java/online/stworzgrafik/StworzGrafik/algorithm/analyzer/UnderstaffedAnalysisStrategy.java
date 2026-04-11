package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UnderstaffedAnalysisStrategy implements ScheduleAnalysisStrategy{
    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.UNDERSTAFFED;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        return new UnderstaffedAnalysisResult(employees,shifts);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((UnderstaffedAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        List<Employee> availableEmployees = ((UnderstaffedAnalysisResult) result).availableEmployees();
        List<Shift> shifts = ((UnderstaffedAnalysisResult) result).shifts();

        Set<Employee> employees = new HashSet<>(context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>()).keySet());

        int i = 1;
        while (shifts.size() > availableEmployees.size()) {
            log.info("          wchodze w resolve understaffed po raz {}", i);
            boolean resolved = modifyProposalToCoverUnmatchedShift(employees, shifts, context, day);
            i++;
            if (!resolved) break;
        }
    }

    private boolean modifyProposalToCoverUnmatchedShift(Set<Employee> employees , List<Shift> shifts, ScheduleGeneratorContext context, LocalDate day){
        Map<Employee, Integer> proposalShiftCount = calculateEmployeeProposalShiftCount(employees, context);

        Optional<Employee> employeeWithMostProposals = proposalShiftCount.entrySet().stream()
                .sorted((key1, key2) -> key2.getValue().compareTo(key1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (k1, k2) -> k1,
                        HashMap::new
                ))
                .keySet().stream()
                .findFirst();

        if (employeeWithMostProposals.isEmpty()) {
            log.info("Nie można znaleźć pracownika z największa liczbą propozycji zmian");

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z największą liczbą propozycji zmian",
                            null,
                            day
                    )
            );
            return false;
        }

        Employee chosenMostProposalsCountEmployee = employeeWithMostProposals.get();

        int[] chosenEmployeeProposal = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>()).getOrDefault(chosenMostProposalsCountEmployee, new int[24]);
        Shift chosenEmployeeShift = context.findShiftByArray(chosenEmployeeProposal);

        Optional<Shift> unmatchedShift = shifts.stream()
                .max(Comparator.comparingInt(
                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
        ));

        if (unmatchedShift.isEmpty()) {
            log.info("Nie można znaleźć zmiany");

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Nie można znaleźć zmiany",
                            null,
                            day
                    )
            );
            return false;
        }

        Shift joinedShift = joinShifts(context, chosenEmployeeShift, unmatchedShift.get());

        context.updateShiftOnSchedule(day,chosenMostProposalsCountEmployee,joinedShift);
        context.updateEmployeeDailyProposal(chosenMostProposalsCountEmployee,day,context.shiftAsArray(joinedShift));
        context.updateEmployeeHours(chosenMostProposalsCountEmployee,chosenEmployeeShift,joinedShift);
        
        shifts.remove(unmatchedShift.get());
        employees.remove(chosenMostProposalsCountEmployee);

        return true;
    }

    private Shift joinShifts(ScheduleGeneratorContext context, Shift firstShift, Shift secondShift){
        LocalTime startHour = (firstShift.getStartHour().getHour() < secondShift.getStartHour().getHour()) ? firstShift.getStartHour() : secondShift.getStartHour();
        LocalTime endHour = (firstShift.getEndHour().getHour() > secondShift.getEndHour().getHour()) ? firstShift.getEndHour() : secondShift.getEndHour();

        return context.findShiftByHours(startHour,endHour);
    }

    private Map<Employee, Integer> calculateEmployeeProposalShiftCount(Set<Employee> employees, ScheduleGeneratorContext context){
        Map<Employee, Integer> dayWithProposalCount = new HashMap<>();

        for (Employee employee : employees) {
            int value = 0;

            for (Map.Entry<LocalDate, Map<Employee, int[]>> entry : context.getMonthlyEmployeesProposalShiftsByDate().entrySet()) {
                Map<Employee, int[]> map = entry.getValue();

                if (map.containsKey(employee) && Arrays.stream(map.getOrDefault(employee,new int[24])).sum() > 0){
                    value++;
                }
            }

            dayWithProposalCount.put(employee,value);
        }

        return dayWithProposalCount;
    }
}
