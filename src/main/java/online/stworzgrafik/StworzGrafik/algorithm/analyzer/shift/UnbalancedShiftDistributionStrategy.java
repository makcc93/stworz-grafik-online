package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
@Service
@Slf4j
public class UnbalancedShiftDistributionStrategy implements ScheduleAnalysisStrategy {

    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.UNBALANCED_SHIFT_DISTRIBUTION;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day,
                                          List<Shift> shifts, List<Employee> employees) {
        Map<Employee, Set<LocalDate>> employeesTooManyDaysOffInARow = new HashMap<>();
        Map<LocalDate, Map<Employee, Shift>> finalSchedule = context.getFinalSchedule();

        List<LocalDate> sortedDates = new ArrayList<>(finalSchedule.keySet());
        Collections.sort(sortedDates);

        for (Employee employee : context.getStoreActiveEmployees()) {
            int consecutiveOff = 0;
            Set<LocalDate> offDays = new HashSet<>();

            for (LocalDate date : sortedDates) {
                Map<Employee, Shift> dailyShifts = finalSchedule.getOrDefault(date, Map.of());
                Shift shift = dailyShifts.getOrDefault(employee, context.getDefaultDaysOffShift());

                boolean isWorking = shift != null && context.getShiftLength(shift).compareTo(BigDecimal.ZERO) > 0;

                if (isWorking) {
                    consecutiveOff = 0;
                } else {
                    consecutiveOff++;
                    if (consecutiveOff > 3) {
                        for (int i = 0; i < consecutiveOff; i++) {
                            offDays.add(date.minusDays(i));
                        }
                    }
                }
            }

            if (!offDays.isEmpty()) {
                employeesTooManyDaysOffInARow.put(employee, offDays);
            }
        }

        return new UnbalancedShiftDistributionResult(employeesTooManyDaysOffInARow);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((UnbalancedShiftDistributionResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        log.info("NIERÓWNOMIERNE ROZŁOŻENIE ZMIAN, WDRAŻAM DZIAŁANIE");
        List<Predicate<Employee>> rolesInOrder = List.of(
                isManagerRole(),
                isCreditRoleOnly(),
                isCheckoutRoleOnly(),
                isRestRoleEmployee()
        );

        ScheduleAnalysisResult currentResult = result;

        for (Predicate<Employee> role : rolesInOrder) {
            if (!hasProblem(currentResult)) {
                return;
            }
            tryResolveForRole(currentResult, context, role, day);
            currentResult = analyze(context, day, null, null);
        }
    }

    private Predicate<Employee> isManagerRole() {
        return Employee::isCanOpenCloseStore;
    }

    private Predicate<Employee> isCheckoutRoleOnly() {
        return e -> e.isCanOperateCheckout() && !e.isCanOpenCloseStore() && !e.isCanOperateCredit();
    }

    private Predicate<Employee> isCreditRoleOnly() {
        return e -> e.isCanOperateCredit() && !e.isCanOpenCloseStore() && !e.isCanOperateCheckout();
    }

    private Predicate<Employee> isRestRoleEmployee() {
        return e -> !e.isCanOpenCloseStore() && !e.isCanOperateCredit() && !e.isCanOperateCheckout() && !e.isWarehouseman();
    }

    private boolean tryResolveForRole(ScheduleAnalysisResult result, ScheduleGeneratorContext context,
                                      Predicate<Employee> roleFilter, LocalDate day) {
        int timesToRepeat = 10;
        boolean anySwapDoneOverall = false;
        ScheduleAnalysisResult currentResult = result;

        for (int attempt = 0; attempt < timesToRepeat; attempt++) {

            Map<Employee, Set<LocalDate>> daysOffInARow = ((UnbalancedShiftDistributionResult) currentResult).daysOffInARow();

            List<Employee> employeesWithManyDaysInARow = context.getStoreActiveEmployees().stream()
                    .filter(roleFilter)
                    .filter(daysOffInARow::containsKey)
                    .toList();

            if (employeesWithManyDaysInARow.isEmpty()) {
                break;
            }

            List<LocalDate> sortedDates = new ArrayList<>(context.getFinalSchedule().keySet());
            Collections.sort(sortedDates);

            boolean progressThisPass = false;

            for (Employee employeeWithManyDaysOff : employeesWithManyDaysInARow) {

                log.info("[UnbalancedShiftDistributionStrategy przebieg {}] Pracownik: {}, Dni wolne: {}",
                        attempt + 1, employeeWithManyDaysOff.getLastName(),
                        daysOffInARow.get(employeeWithManyDaysOff).toArray());

                boolean swapped = tryResolveSingleEmployee(
                        employeeWithManyDaysOff,
                        daysOffInARow.get(employeeWithManyDaysOff),
                        context, roleFilter, sortedDates);

                if (swapped) {
                    progressThisPass = true;
                    anySwapDoneOverall = true;
                }

                log.info("[UnbalancedShiftDistributionStrategy] KONIEC PRACOWNIKA {}", employeeWithManyDaysOff.getLastName());
            }

            if (!progressThisPass) {
                log.info("[UnbalancedShiftDistributionStrategy] Brak progresu w przebiegu {} - przerywam.", attempt + 1);
                break;
            }

            currentResult = analyze(context, day, null, null);
            if (!hasProblem(currentResult)) {
                break;
            }
        }

        return anySwapDoneOverall;
    }

    private boolean tryResolveSingleEmployee(Employee employeeWithManyDaysOff, Set<LocalDate> daysOff,
                                             ScheduleGeneratorContext context, Predicate<Employee> roleFilter,
                                             List<LocalDate> sortedDates) {

        List<Employee> otherActiveEmployees = context.getStoreActiveEmployees().stream()
                .filter(roleFilter)
                .filter(e -> !e.equals(employeeWithManyDaysOff))
                .toList();

        for (LocalDate date : daysOff) {

            if (context.employeeHasProposalDaysOff(employeeWithManyDaysOff, date)) continue;
            if (context.isEmployeeOnRestRequirementDayOff(employeeWithManyDaysOff, date)) continue;
            if (context.employeeIsOnVacation(employeeWithManyDaysOff, date)) continue;
            if (context.employeeIsOnDelegation(employeeWithManyDaysOff, date)) continue;

            Map<Employee, Shift> dailyShiftsOnDate = context.getFinalSchedule().getOrDefault(date, Map.of());

            for (Employee otherEmployee : otherActiveEmployees) {
                Shift otherEmployeeShiftOnDate = dailyShiftsOnDate.getOrDefault(otherEmployee, context.getDefaultDaysOffShift());

                if (context.getShiftLength(otherEmployeeShiftOnDate).compareTo(BigDecimal.ZERO) <= 0) continue;
                if (context.employeeIsOnVacation(otherEmployee, date)) continue;
                if (context.employeeIsOnDelegation(otherEmployee, date)) continue;

                for (LocalDate dayOff : sortedDates) {
                    if (dayOff.equals(date)) continue;
                    if (dayOff.getDayOfWeek() == DayOfWeek.SATURDAY || dayOff.getDayOfWeek() == DayOfWeek.SUNDAY)
                        continue;

                    Map<Employee, Shift> dailyShiftsOnDayOff = context.getFinalSchedule().getOrDefault(dayOff, Map.of());
                    Shift employeeShiftOnDayOff = dailyShiftsOnDayOff.getOrDefault(employeeWithManyDaysOff, context.getDefaultDaysOffShift());
                    Shift otherEmployeeShiftOnDayOff = dailyShiftsOnDayOff.getOrDefault(otherEmployee, context.getDefaultDaysOffShift());

                    boolean employeeWorksOnDayOff = context.getShiftLength(employeeShiftOnDayOff).compareTo(BigDecimal.ZERO) > 0;
                    boolean otherEmployeeOffOnDayOff = context.getShiftLength(otherEmployeeShiftOnDayOff).compareTo(BigDecimal.ZERO) <= 0;

                    if (!employeeWorksOnDayOff || !otherEmployeeOffOnDayOff) continue;

                    if (context.employeeIsOnVacation(employeeWithManyDaysOff, dayOff)) continue;
                    if (context.employeeIsOnDelegation(employeeWithManyDaysOff, dayOff)) continue;
                    if (context.employeeHasProposalDaysOff(employeeWithManyDaysOff, dayOff)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(employeeWithManyDaysOff, dayOff)) continue;

                    if (context.employeeIsOnVacation(otherEmployee, dayOff)) continue;
                    if (context.employeeIsOnDelegation(otherEmployee, dayOff)) continue;
                    if (context.employeeHasProposalDaysOff(otherEmployee, dayOff)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(otherEmployee, dayOff)) continue;

                    if (shiftSwapCreatesNewProblem(employeeWithManyDaysOff, dayOff, date, context)) continue;
                    if (shiftSwapCreatesNewProblem(otherEmployee, date, dayOff, context)) continue;

                    context.updateShiftOnSchedule(dayOff, employeeWithManyDaysOff, context.getDefaultDaysOffShift());
                    context.updateShiftOnSchedule(date, employeeWithManyDaysOff, otherEmployeeShiftOnDate);

                    context.updateShiftOnSchedule(dayOff, otherEmployee, employeeShiftOnDayOff);
                    context.updateShiftOnSchedule(date, otherEmployee, context.getDefaultDaysOffShift());

                    return true;
                }
            }
        }

        return false;
    }

    private boolean shiftSwapCreatesNewProblem(Employee employee, LocalDate newDateWithDayOff, LocalDate newDateWithWork,
                                               ScheduleGeneratorContext context) {
        int consecutiveOff = 0;
        Shift defaultDaysOffShift = context.getDefaultDaysOffShift();

        for (int i = -3; i <= 3; i++) {
            LocalDate day = newDateWithDayOff.plusDays(i);

            Map<Employee, Shift> dailyShifts = day.isEqual(newDateWithDayOff)
                    ? Map.of(employee, defaultDaysOffShift)
                    : context.getFinalSchedule().getOrDefault(day, Map.of());
            Shift shift = dailyShifts.getOrDefault(employee, defaultDaysOffShift);

            boolean isWorking = day.isEqual(newDateWithWork)
                    || (shift != null && context.getShiftLength(shift).compareTo(BigDecimal.ZERO) > 0)
                    || day.getMonth().getValue() != context.getMonth();

            if (isWorking) {
                consecutiveOff = 0;
            } else {
                consecutiveOff++;
                if (consecutiveOff > 3) {
                    log.info("[UnbalancedShiftDistributionStrategy] Zamiana dni utworzy nowy problem podczas rozwiązania starego. Pracownik: {}, Data zmiany na wolne: {}. Przerywam",
                            employee.getLastName(), newDateWithDayOff);
                    return true;
                }
            }
        }
        return false;
    }
}