package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftSplitterAnalysisStrategy implements ScheduleAnalysisStrategy {
    private final CalendarCalculation calendarCalculation;
    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.SHIFT_SPLITTER;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(),context.getMonth());
        LinkedHashMap<Employee,Integer> workingDaysCount = new LinkedHashMap<>();

        for (Employee employee : employees) {
            Integer value = context.getWorkingDaysCount().getOrDefault(employee, 0);

            workingDaysCount.put(employee,value);
        }

        LinkedHashMap<Employee, Integer> filteredWorkingDaysCountSortedAsc = workingDaysCount.entrySet().stream()
                .filter(entry -> entry.getKey().isWarehouseman())
                .filter(entry -> entry.getKey().isCashier())
                .sorted((k1, k2) -> k1.getValue().compareTo(k2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        if (filteredWorkingDaysCountSortedAsc.isEmpty()){
            log.info("Pusta mapa z pracowniczą liczbą dni pracy");
        }

        Integer lowestValue = filteredWorkingDaysCountSortedAsc.entrySet().stream().findFirst().get().getValue();

       return new ShiftSplitterAnalysisResult(monthlyMaxWorkingDays,lowestValue);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((ShiftSplitterAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        int monthlyMaxWorkingDays = ((ShiftSplitterAnalysisResult) result).monthlyMaxWorkingDays();
        int employeeLowestValueOfWorkingDays = ((ShiftSplitterAnalysisResult) result).employeeLowestValueOfWorkingDays();

        while(employeeLowestValueOfWorkingDays <= monthlyMaxWorkingDays - 2){
            boolean resolved = splitShifts(context);

            if (!resolved) break;
        }
    }

    private boolean splitShifts(ScheduleGeneratorContext context){
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        managersSplitShifts(context,monthlyMaxWorkingDays);
        operateCreditSplitShifts();
        othersSplitShifts();

        return true;
    }

    private boolean managersSplitShifts(ScheduleGeneratorContext context, int monthlyMaxWorkingDays){
        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(Employee::isCanOpenCloseStore)
                .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                .toList();

        Map<Employee,Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();
        Map<LocalDate, Employee> employeeDaysOff = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(context.getYear(),context.getMonth());

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(context.getYear(),context.getMonth(),day);

            for (Employee employee : employees){
                if (!context.employeeIsWorking(employee,date)) break;
                if (context.employeeIsOnVacation(employee,day)) break;
                if (context.employeeHasProposalShift(employee,date)) break;
                if (context.employeeHasProposalDaysOff(employee,date)) break;
                if (context.employeeIsOnDayOff(employee,day)){
                    employeeDaysOff.put(date,employee);

                    break;
                }

                Shift shift = context.getFinalSchedule().getOrDefault(date, new HashMap<>()).getOrDefault(employee, context.findShiftByArray(new int[24]));

                daysWithShiftsToSplit.put(employee, Map.of(date,shift));
            }
        }

        LinkedHashMap<Employee, Map<LocalDate, Shift>> daysWithShiftsSortedByLengthDesc = daysWithShiftsToSplit.entrySet().stream()
                // 1. sortujemy wpisy zewnętrznej mapy po liczbie dni pracy rosnąco
                .sorted(Comparator.comparingInt(entry ->
                        context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0)))
                // 2. zbieramy z powrotem do mapy, transformując wewnętrzną mapę
                .collect(Collectors.toMap(
                        Map.Entry::getKey,   // klucz: Employee (bez zmian)
                        entry -> entry.getValue().entrySet().stream()
                                // 3. sortujemy wewnętrzne wpisy po długości zmiany malejąco
                                .sorted((a, b) -> getShiftLength(b.getValue()) - getShiftLength(a.getValue()))
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,   // klucz: LocalDate
                                        Map.Entry::getValue, // wartość: Shift
                                        (v1, v2) -> v1,      // merge function (nie wystąpi, LocalDate jest unikalny)
                                        LinkedHashMap::new   // zachowuje kolejność sortowania
                                )),
                        (v1, v2) -> v1,      // merge function dla zewnętrznej mapy (też nie wystąpi)
                        LinkedHashMap::new   // zewnętrzna mapa też musi zachować kolejność
                ));

    }

    private int getShiftLength(Shift shift){
        return shift.getEndHour().getHour() - shift.getStartHour().getHour();
    }
}
