package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UnderstaffedAnalysisStrategy implements ScheduleAnalysisStrategy{
    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.UNDERSTAFFED;
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
        log.info("SPRAWDZENIE ZBYT MAŁEJ ILOŚCI DOSTĘPNYCH PRACOWNIKÓW");
        List<Employee> availableEmployees = ((UnderstaffedAnalysisResult) result).availableEmployees();
        List<Shift> shifts = ((UnderstaffedAnalysisResult) result).shifts();

        Set<Employee> employees = new HashSet<>(
                context.getMonthlyEmployeesProposalShiftsByDate()
                        .getOrDefault(day, new HashMap<>()).keySet()
        );

        while (shifts.size() > employees.size()) {
            if (employees.isEmpty()) {
                log.warn("Brak dostępnych pracowników dla dnia: {}", day);
                break;
            }

            boolean resolved = modifyEmployeeProposalToCoverUnmatchedShift(employees, shifts, context, day);
            if (!resolved) break;
        }
    }

    private boolean modifyEmployeeProposalToCoverUnmatchedShift(Set<Employee> employees , List<Shift> shifts, ScheduleGeneratorContext context, LocalDate day) {
        Shift shiftToCover = shifts.getFirst();
        int[] shiftToCoverAsArray = context.shiftAsArray(shiftToCover);

        Map<Employee,Integer> shiftMatchingScore = new HashMap<>();
        Map<Employee,Shift> candidateJoinedShift = new HashMap<>();
        Map<Employee,BigDecimal> candidateAdditionalHours = new HashMap<>();

        for (Employee employee : employees){
            int[] employeeProposalAsArray = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>()).getOrDefault(employee, new int[24]);

            int[] summedArray = summedArrays(shiftToCoverAsArray,employeeProposalAsArray);

            int score = calculateMatchingScore(summedArray);
            shiftMatchingScore.put(employee,score);

            Shift currentEmployeeShift = context.findShiftByArray(employeeProposalAsArray);
            Shift joinedShift = joinShifts(context, currentEmployeeShift, shiftToCover);
            BigDecimal additionalHours = context.getShiftLength(joinedShift).subtract(context.getShiftLength(currentEmployeeShift));

            candidateJoinedShift.put(employee, joinedShift);
            candidateAdditionalHours.put(employee, additionalHours);
        }

        Comparator<Employee> byMatchingScoreDesc = Comparator.comparing(
                (Employee e) -> shiftMatchingScore.getOrDefault(e, 0)
        ).reversed();

        Comparator<Employee> comparator = context.isLastMonthOfPeriod()
                ? Comparator.<Employee>comparingInt(e ->
                        context.wouldExceedHoursLimit(e, candidateAdditionalHours.get(e)) ? 1 : 0)
                .thenComparing(byMatchingScoreDesc)
                : byMatchingScoreDesc;

        Optional<Employee> highestMatchingScoreEmployee = employees.stream().min(comparator);

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
        Shift joinedShift = candidateJoinedShift.get(chosenEmployee);
        BigDecimal additionalHours = candidateAdditionalHours.get(chosenEmployee);

        if (context.isLastMonthOfPeriod() && context.wouldExceedHoursLimit(chosenEmployee, additionalHours)) {
            log.warn("Dołożenie brakującej zmiany dla {} {} w dniu {} przekroczy zatwierdzony limit godzin o {}h (ostatni miesiąc okresu) - wykonuję mimo to, bo zmiana musi zostać obsadzona.",
                    chosenEmployee.getFirstName(), chosenEmployee.getLastName(), day, additionalHours);

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.EMPLOYEE_MONTHLY_SUM_OF_HOURS_EXCEEDED,
                            "Dołożenie brakującej zmiany dla " + chosenEmployee.getFirstName() + " " + chosenEmployee.getLastName() +
                                    " w dniu " + day + " przekracza zatwierdzony limit godzin.",
                            chosenEmployee.getId(),
                            day
                    )
            );
        }

        context.updateShiftOnSchedule(day, chosenEmployee, joinedShift);
        context.updateEmployeeDailyProposal(chosenEmployee, day, context.shiftAsArray(joinedShift));

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

        return result;
    }

    private Shift joinShifts(ScheduleGeneratorContext context, Shift firstShift, Shift secondShift){
        LocalTime startHour = (firstShift.getStartHour().getHour() < secondShift.getStartHour().getHour()) ? firstShift.getStartHour() : secondShift.getStartHour();
        LocalTime endHour = (firstShift.getEndHour().getHour() > secondShift.getEndHour().getHour()) ? firstShift.getEndHour() : secondShift.getEndHour();

        return context.findShiftByHours(startHour,endHour);
    }
}
