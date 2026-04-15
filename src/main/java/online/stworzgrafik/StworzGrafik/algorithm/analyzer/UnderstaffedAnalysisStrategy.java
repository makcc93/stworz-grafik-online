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

        while (shifts.size() > availableEmployees.size()) {
            boolean resolved = modifyEmployeeProposalToCoverUnmatchedShift(employees, shifts, context, day);

            if (!resolved) break;
        }
    }

    private boolean modifyEmployeeProposalToCoverUnmatchedShift(Set<Employee> employees , List<Shift> shifts, ScheduleGeneratorContext context, LocalDate day) {
        Shift shiftToCover = shifts.getFirst();
        int[] shiftToCoverAsArray = context.shiftAsArray(shiftToCover);

        log.info("  Zmiana, którą trzeba dopasować: {}-{}\n", shiftToCover.getStartHour(),shiftToCover.getEndHour());

        Map<Employee,Integer> shiftMatchingScore = new HashMap<>();
        for (Employee employee : employees){
            log.info("---");
            int[] employeeProposalAsArray = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>()).getOrDefault(employee, new int[24]);
            Shift employeeProposalShift = context.findShiftByArray(employeeProposalAsArray);

            log.info("EMPL: {} {} | SHIFT: {}-{}",
                    employee.getFirstName(),employee.getLastName(),
                    employeeProposalShift.getStartHour(),
                    employeeProposalShift.getEndHour());

            int[] summedArray = summedArrays(shiftToCoverAsArray,employeeProposalAsArray);

            int score = calculateMatchingScore(summedArray);

            shiftMatchingScore.put(employee,score);
            log.info("SCORING: {}",
                    score);

            log.info("---");
        }
        log.info("");

        Optional<Employee> highestMatchingScoreEmployee = shiftMatchingScore.entrySet().stream()
                .sorted((key1, key2) -> key2.getValue().compareTo(key1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (k1, k2) -> k1,
                        LinkedHashMap::new
                ))
                .keySet().stream()
                .findFirst();

        if (highestMatchingScoreEmployee.isEmpty()) {
            log.info("Nie można znaleźć pracownika z najwyższym wynikiem dopasowania do zmiany");

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z najwyższym wynikiem dopasowania do zmiany",
                            null,
                            day
                    )
            );
            return false;
        }

        Employee chosenEmployee = highestMatchingScoreEmployee.get();

        int[] employeeProposalAsArray = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>()).getOrDefault(chosenEmployee, new int[24]);
        Shift chosenEmployeeShift = context.findShiftByArray(employeeProposalAsArray);

        log.info("  Najlepiej dopasowana zmiana do połączenia: {}-{} u {} {}",
                chosenEmployeeShift.getStartHour(),
                chosenEmployeeShift.getEndHour(),
                chosenEmployee.getFirstName(),
                chosenEmployee.getLastName());

        Shift joinedShift = joinShifts(context, chosenEmployeeShift, shiftToCover);

        context.updateShiftOnSchedule(day, chosenEmployee,joinedShift);
        context.updateEmployeeDailyProposal(chosenEmployee,day,context.shiftAsArray(joinedShift));
        context.updateEmployeeHours(chosenEmployee,chosenEmployeeShift,joinedShift);

        shifts.remove(shiftToCover);
        employees.remove(chosenEmployee);

        return true;
    }

    private int calculateMatchingScore(int[] summedArray){
        int score = 0;

        for (int i = 0; i < 24; i++){
            if (summedArray[i] == 1) score++;
        }

        return score;
    }

    private int[] summedArrays(int[] shiftToCoverAsArray, int[] employeeProposalAsArray){
        int[] result =  new int[24];

        for (int i = 0; i < 24; i++){
            result[i] = shiftToCoverAsArray[i] + employeeProposalAsArray[i];
        }
        log.info("summedArrays shiftToCover:     {}",shiftToCoverAsArray);
        log.info("summedArrays employeeProposal: {}", employeeProposalAsArray);
        log.info("summedArrays RESULT:           {}", result);
        return result;
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
