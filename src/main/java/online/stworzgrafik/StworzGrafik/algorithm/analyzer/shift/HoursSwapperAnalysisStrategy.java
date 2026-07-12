package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoursSwapperAnalysisStrategy implements ScheduleAnalysisStrategy {
    private final HolidayManager holidayManager;

    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.HOURS_SWAPPER;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        BigDecimal maxHoursDifference = BigDecimal.valueOf(1);

        // === ZMIANA: porównanie WZGLĘDEM indywidualnego limitu (getRemainingHoursUntilLimit),
        // a nie surowych godzin (context.getEmployeeHours()) - patrz komentarz przy
        // ScheduleGeneratorContext.getRemainingHoursUntilLimit(). Pracownik z najmniejszym
        // zapasem do limitu (najmniej "miejsca") ląduje w polu "highest" (bo to on jest
        // najbardziej wykorzystany), pracownik z największym zapasem - w polu "lowest".
        // Przy równych limitach matematycznie daje to dokładnie ten sam wynik co poprzednio
        // (surowe godziny), więc nic się nie zmienia poza ostatnim miesiącem okresu / part-time.
        BigDecimal employeeLowestValueOfWorkingHours = employees.stream()
                .filter(empl -> !empl.isWarehouseman())
                .sorted(Comparator.comparing(
                        (Employee e) -> context.getRemainingHoursUntilLimit(e)
                ))
                .findFirst()
                .map(context::getRemainingHoursUntilLimit)
                .orElse(BigDecimal.ZERO);

        BigDecimal employeeHighestValueOfWorkingHours = employees.stream()
                .filter(empl -> !empl.isWarehouseman())
                .sorted(Comparator.comparing(
                        (Employee e) -> context.getRemainingHoursUntilLimit(e)
                ).reversed())
                .findFirst()
                .map(context::getRemainingHoursUntilLimit)
                .orElse(BigDecimal.ZERO);

        return new HoursSwapperAnalysisResult(employeeLowestValueOfWorkingHours,employeeHighestValueOfWorkingHours,maxHoursDifference);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((HoursSwapperAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        log.info("PRÓBA PODMIANY GODZIN");
        BigDecimal maxHoursDifference = ((HoursSwapperAnalysisResult) result).maxHoursDifference();

        int maxIterations = 50;
        int iteration = 0;

        while (true) {
            if (++iteration > maxIterations) {
                log.warn("Osiągnięto maksymalną liczbę iteracji ({}) przy próbie podmiany godzin - przerywam, aby uniknąć zapętlenia", maxIterations);
                break;
            }

            // === ZMIANA: to samo co w analyze() - porównujemy dystans do limitu
            // (getRemainingHoursUntilLimit), nie surowe godziny entry.getValue().
            BigDecimal employeeLowestValueOfWorkingHours = context.getEmployeeHours().entrySet().stream()
                    .filter(entry -> !entry.getKey().isWarehouseman())
                    .filter(entry -> !entry.getKey().isCashier())
                    .sorted(Comparator.comparing(
                            (Map.Entry<Employee, BigDecimal> entry) -> context.getRemainingHoursUntilLimit(entry.getKey())
                    ))
                    .findFirst()
                    .map(entry -> context.getRemainingHoursUntilLimit(entry.getKey()))
                    .orElse(BigDecimal.ZERO);

            BigDecimal employeeHighestValueOfWorkingHours = context.getEmployeeHours().entrySet().stream()
                    .filter(entry -> !entry.getKey().isWarehouseman())
                    .filter(entry -> !entry.getKey().isCashier())
                    .sorted(Comparator.comparing(
                            (Map.Entry<Employee, BigDecimal> entry) -> context.getRemainingHoursUntilLimit(entry.getKey())
                    ).reversed())
                    .findFirst()
                    .map(entry -> context.getRemainingHoursUntilLimit(entry.getKey()))
                    .orElse(BigDecimal.ZERO);

            if ((employeeHighestValueOfWorkingHours.subtract(employeeLowestValueOfWorkingHours)).compareTo(maxHoursDifference) <= 0) {
                log.info("Różnica godzin w normie, kończę podmianę.");
                break;
            }

            boolean anySwapDone = swapHours(context);

            if (!anySwapDone) {
                log.info("Brak możliwych zamian, kończę podmianę.");
                break;
            }
        }
    }

    private boolean swapHours(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        List<Employee> employees = context.getStoreNotSpecialActiveEmployees();
        int timesToRepeat = 5;

        YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());


        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);

                if (holidayManager.isHoliday(date) || Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum() < 1)
                    continue;

                Map<Employee, BigDecimal> employeeHours = new HashMap<>();
                Map<Employee, Shift> employeeShift = new HashMap<>();

                for (Employee employee : employees) {
                    if (employee.isCashier())  continue;
                    if (employee.isWarehouseman()) continue;
                    if (context.employeeHasProposalShift(employee, date)) continue;
                    if (context.employeeHasProposalDaysOff(employee, date)) continue;
                    if (context.isEmployeeWorkingInWarehouse(employee, date)) continue;
                    if (context.employeeIsOnVacation(employee, date)) continue;
                    if (context.employeeIsOnDelegation(employee, date)) continue;
                    if (context.isEmployeeWorkingOnCredit(employee, date)) continue;
                    if (context.isEmployeeWorkingOnCheckout(employee,date)) continue;
                    if (context.isOpeningOrClosingStore(employee,date)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(employee,date)) continue;
                    if (!context.employeeIsWorking(employee, date)) continue;

                    employeeHours.put(employee, context.getEmployeeHours().getOrDefault(employee, BigDecimal.ZERO));

                    Shift shift = context.getFinalSchedule().getOrDefault(date, new HashMap<>()).getOrDefault(employee, context.getDefaultDaysOffShift());

                    employeeShift.put(employee, shift);
                }

                if (employeeShift.size() < 2) continue;

                // === ZMIANA: highestHoursEmployee to teraz pracownik z NAJMNIEJSZYM
                // zapasem do własnego limitu (najmniej "miejsca" - to on oddaje dłuższą
                // zmianę), a nie z największymi surowymi godzinami. Sortujemy rosnąco po
                // getRemainingHoursUntilLimit (pierwszy = najmniejszy zapas).
                Employee highestHoursEmployee = employeeHours.entrySet().stream()
                        .sorted(Comparator.comparing(
                                (Map.Entry<Employee, BigDecimal> entry) -> context.getRemainingHoursUntilLimit(entry.getKey())
                        ))
                        .map(Map.Entry::getKey)
                        .toList()
                        .getFirst();

                BigDecimal highestEmployeeHoursCount = employeeHours.getOrDefault(highestHoursEmployee, BigDecimal.ZERO);
                Shift highestHoursEmployeeShift = employeeShift.getOrDefault(highestHoursEmployee, context.getDefaultDaysOffShift());
                BigDecimal highestHoursEmployeeShiftLength = context.getShiftLength(highestHoursEmployeeShift);

                // === ZMIANA: lowestHoursEmployee to pracownik z NAJWIĘKSZYM zapasem do
                // własnego limitu (najwięcej "miejsca" - to on dostaje dłuższą zmianę).
                // Sortujemy malejąco po getRemainingHoursUntilLimit (pierwszy = największy zapas).
                Employee lowestHoursEmployee = employeeHours.entrySet().stream()
                        .sorted(Comparator.comparing(
                                (Map.Entry<Employee, BigDecimal> entry) -> context.getRemainingHoursUntilLimit(entry.getKey())
                        ).reversed())
                        .map(Map.Entry::getKey)
                        .toList()
                        .getFirst();

                BigDecimal lowestEmployeeHoursCount = employeeHours.getOrDefault(lowestHoursEmployee, BigDecimal.ZERO);
                Shift lowestHoursEmployeeShift = employeeShift.getOrDefault(lowestHoursEmployee, context.getDefaultDaysOffShift());
                BigDecimal lowestHoursEmployeeShiftLength = context.getShiftLength(lowestHoursEmployeeShift);

                if ((highestHoursEmployeeShiftLength.compareTo(lowestHoursEmployeeShiftLength) > 0) &&
                        (highestEmployeeHoursCount.subtract(lowestEmployeeHoursCount)).compareTo(highestHoursEmployeeShiftLength.subtract(lowestHoursEmployeeShiftLength)) > 0) {

                    if (context.isLastMonthOfPeriod() &&
                            context.wouldExceedHoursLimit(lowestHoursEmployee, highestHoursEmployeeShiftLength.subtract(lowestHoursEmployeeShiftLength))) {
                        continue;
                    }

                    context.updateShiftOnSchedule(date, highestHoursEmployee, lowestHoursEmployeeShift);
                    context.updateShiftOnSchedule(date, lowestHoursEmployee, highestHoursEmployeeShift);
                    anySwapDone = true;
                }
            }
        }
        return anySwapDone;
    }
}