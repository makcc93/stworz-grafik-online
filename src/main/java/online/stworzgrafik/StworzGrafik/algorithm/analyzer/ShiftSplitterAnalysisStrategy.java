package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.ShiftSwapCandidate;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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

        LinkedHashMap<Employee,Integer> workingDaysCount = new LinkedHashMap<>();

        for (Employee employee : employees) {
            Integer value = context.getWorkingDaysCount().getOrDefault(employee, 0);
            log.info("value = {}", value);
            log.info("employee = {}", employee);

            workingDaysCount.put(employee,value);
        }

        LinkedHashMap<Employee, Integer> filteredWorkingDaysCountSortedAsc = workingDaysCount.entrySet().stream()
                .filter(entry -> !entry.getKey().isWarehouseman())
                .filter(entry -> !entry.getKey().isCashier())
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
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

        log.info("analyzer przekazuje, lowestValue = {}, maxWorkingDays = {}",lowestValue,monthlyMaxWorkingDays);
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
        log.info("*** wchodze w resolve, max days: {}", monthlyMaxWorkingDays);

        while (context.getWorkingDaysCount().entrySet().stream()
                .filter(e -> e.getKey().isCanOpenCloseStore())
                .anyMatch(e -> e.getValue() <= monthlyMaxWorkingDays - 2)) {
            boolean resolved = splitShifts(context);

            if (!resolved) break;
        }

        log.info("*** koncze resolve");
    }

    private boolean splitShifts(ScheduleGeneratorContext context){
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        boolean result = managersSplitShifts(context,monthlyMaxWorkingDays);
//        operateCreditSplitShifts();
//        othersSplitShifts();

        return result;
    }

    private boolean managersSplitShifts(ScheduleGeneratorContext context, int monthlyMaxWorkingDays) {
        boolean anySwapDone = false;

        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(Employee::isCanOpenCloseStore)
                .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                .toList();

        Map<Employee, Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);

            for (Employee employee : employees) {
                if (!context.employeeIsWorking(employee, date)) continue;
                if (context.employeeIsOnVacation(employee, day)) continue;
                if (context.employeeHasProposalShift(employee, date)) continue;
                if (context.employeeHasProposalDaysOff(employee, date)) continue;
                if (context.employeeIsOnUnwantedDayOff(employee, day)) continue;

                Shift shift = context.getFinalSchedule().getOrDefault(date, new HashMap<>()).getOrDefault(employee, context.findShiftByArray(new int[24]));

                daysWithShiftsToSplit
                        .computeIfAbsent(employee, k -> new HashMap<>())
                        .put(date, shift);
            }
        }

        LinkedHashMap<Employee, Map<LocalDate, Shift>> employeeSortedByWorkingDaysAscAndShiftsLengthSortedDesc = daysWithShiftsToSplit.entrySet().stream()
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


        for (Map.Entry<Employee, Map<LocalDate, Shift>> entry : employeeSortedByWorkingDaysAscAndShiftsLengthSortedDesc.entrySet()) {
            Employee employee = entry.getKey();
            Map<LocalDate, Shift> employeeDateShift = entry.getValue();

            List<ShiftSwapCandidate> swapCandidates = employeeDateShift.entrySet().stream()
                    .flatMap(insideEntry -> {
                                LocalDate insideDate = insideEntry.getKey();
                                Shift insideShift = insideEntry.getValue();

                                return employees.stream()
                                        .filter(empl -> !empl.equals(employee))
                                        .filter(empl -> employeeSortedByWorkingDaysAscAndShiftsLengthSortedDesc.containsKey(empl))
                                        .filter(empl -> !context.employeeIsWorking(empl, insideDate))
                                        .flatMap(empl -> employeeSortedByWorkingDaysAscAndShiftsLengthSortedDesc.get(empl).entrySet().stream()
                                                .filter(otherEmpl -> {
                                                    LocalDate otherDate = otherEmpl.getKey();
                                                    Shift otherShift = otherEmpl.getValue();

                                                    return insideShift.equals(otherShift) && !context.employeeIsWorking(employee, otherDate);
                                                })
                                                .map(otherEmpl -> new ShiftSwapCandidate(
                                                        employee,
                                                        empl,
                                                        insideDate,
                                                        otherEmpl.getKey(),
                                                        insideShift
                                                )));
                            }
                    )
                    .toList();


            for (ShiftSwapCandidate candidate : swapCandidates) {
                Employee originalEmployee = candidate.originalEmployee();
                Employee otherEmployeeForSwap = candidate.employeeForSwapShift();
                LocalDate originalEmployeeDate = candidate.originalDateForSwap();
                LocalDate otherEmployeeDateForSwap = candidate.otherEmployeeDateForSwap();

                int originalDays = context.getWorkingDaysCount().getOrDefault(originalEmployee, 0);
                int otherDays = context.getWorkingDaysCount().getOrDefault(otherEmployeeForSwap, 0);

                if (originalDays >= monthlyMaxWorkingDays) break;
                if (otherDays >= monthlyMaxWorkingDays) continue;

                if (context.employeeIsWorking(otherEmployeeForSwap, originalEmployeeDate)) continue;
                if (context.employeeIsWorking(originalEmployee, otherEmployeeDateForSwap)) continue;

                List<Shift> dividedShift = divideShift(candidate.swappingShift(), context);
                Shift firstShift = dividedShift.get(0);
                Shift secondShift = dividedShift.get(1);

                context.updateShiftOnSchedule(originalEmployeeDate, originalEmployee, firstShift);
                context.registerShiftOnSchedule(originalEmployeeDate, otherEmployeeForSwap, secondShift, originalEmployeeDate.getDayOfWeek());

                context.updateShiftOnSchedule(otherEmployeeDateForSwap, otherEmployeeForSwap, firstShift);
                context.registerShiftOnSchedule(otherEmployeeDateForSwap, originalEmployee, secondShift, otherEmployeeDateForSwap.getDayOfWeek());

                anySwapDone = true;

                if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= monthlyMaxWorkingDays) break;
            }
        }
        return anySwapDone;
    }


    private List<Shift> divideShift(Shift shift, ScheduleGeneratorContext context){
        int startHour = shift.getStartHour().getHour();
        int endHour = shift.getEndHour().getHour();

        int midHour = (startHour + endHour) / 2;
        Shift firstShift = context.findShiftByHours(LocalTime.of(startHour, 0), LocalTime.of(midHour, 0));
        Shift secondShift = context.findShiftByHours(LocalTime.of(midHour, 0), LocalTime.of(endHour, 0));
        log.info("*** Podzielona zmana: {}-{} na zmiany: 1. {}-{}, 2. {}-{}", shift.getStartHour(),shift.getEndHour(),firstShift.getStartHour(),firstShift.getEndHour(),secondShift.getStartHour(),secondShift.getEndHour());

        return List.of(firstShift,secondShift);
    }

    private int getShiftLength(Shift shift){
        return shift.getEndHour().getHour() - shift.getStartHour().getHour();
    }
}