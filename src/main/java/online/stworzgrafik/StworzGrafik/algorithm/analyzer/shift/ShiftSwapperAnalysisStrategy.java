package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShiftSwapperAnalysisStrategy implements ScheduleAnalysisStrategy {
    private final CalendarCalculation calendarCalculation;
    private final int timesToRepeat = 3;

    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.SHIFT_SWAPPER;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        BigDecimal minHoursDifference = BigDecimal.valueOf(8);

        BigDecimal lowestHours = employees.stream()
                .filter(e -> !e.isWarehouseman())
                .map(e -> context.getEmployeeHours().getOrDefault(e, BigDecimal.ZERO))
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal highestHours = employees.stream()
                .filter(e -> !e.isWarehouseman())
                .map(e -> context.getEmployeeHours().getOrDefault(e, BigDecimal.ZERO))
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        return new ShiftSwapperAnalysisResult(lowestHours, highestHours, minHoursDifference);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((ShiftSwapperAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate date) {
        log.info("PRÓBA ZAMIANY ZMIAN");
        BigDecimal minHoursDifference = ((ShiftSwapperAnalysisResult) result).minHoursDifference();

        while (true) {
            BigDecimal lowestHours = lowestNonSpecialistHours(context);
            BigDecimal highestHours = highestNonSpecialistHours(context);

            if (highestHours.subtract(lowestHours).compareTo(minHoursDifference) <= 0) break;

            boolean resolved = swapManagerShifts(context);
            resolved |= swapCreditShifts(context);
            resolved |= swapCheckoutShifts(context);
            resolved |= swapOthersShifts(context);

            if (!resolved) break;
        }
    }

    private boolean swapManagerShifts(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year, month);
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (isNotWorkingDay(context, date)) continue;

                Optional<Employee> highestHoursWorkingManager = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> context.isOpeningOrClosingStore(entry.getKey(), date))
                        .filter(entry -> entry.getKey().isCanOpenCloseStore())
                        .filter(entry -> !entry.getKey().isWarehouseman())
                        .filter(entry -> !entry.getKey().isCashier())
                        .filter(entry -> !context.isEmployeeWorkingInWarehouse(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeWorkingOnCredit(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeWorkingOnCheckout(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalShift(entry.getKey(), date))
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue().reversed())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (highestHoursWorkingManager.isEmpty()) continue;

                Optional<Employee> lowestHoursNotWorkingManager = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> entry.getKey().isCanOpenCloseStore())
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingManager.isEmpty()) continue;

                Employee highestHoursManager = highestHoursWorkingManager.get();
                Employee lowestHoursManager = lowestHoursNotWorkingManager.get();
                Shift shift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursManager);
                BigDecimal highestHours = context.getEmployeeHours().getOrDefault(highestHoursManager, BigDecimal.ZERO);
                BigDecimal lowestHours = context.getEmployeeHours().getOrDefault(lowestHoursManager, BigDecimal.ZERO);

                if (lowestEmployeeHasMoreHoursThanHighest(highestHours, lowestHours)) continue;
                if (context.getShiftLength(shift).compareTo(highestHours.subtract(lowestHours)) >= 0) continue;

                executeSwap(context, date, highestHoursManager, lowestHoursManager, shift);
                context.deleteEmployeeToOpenClose(date, highestHoursManager);
                context.assignEmployeeToOpenClose(date, lowestHoursManager, shift);
                anySwapDone = true;
            }
        }
        return anySwapDone;
    }

    private boolean swapCreditShifts(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year, month);
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (isNotWorkingDay(context, date)) continue;

                Optional<Employee> highestHoursWorkingCreditEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> context.isEmployeeWorkingOnCredit(entry.getKey(), date))
                        .filter(entry -> entry.getKey().isCanOperateCredit())
                        .filter(entry -> !entry.getKey().isWarehouseman())
                        .filter(entry -> !entry.getKey().isCashier())
                        .filter(entry -> !context.isEmployeeWorkingInWarehouse(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeWorkingOnCheckout(entry.getKey(), date))
                        .filter(entry -> !context.isOpeningOrClosingStore(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalShift(entry.getKey(), date))
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue().reversed())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (highestHoursWorkingCreditEmployee.isEmpty()) continue;

                Optional<Employee> lowestHoursNotWorkingCreditEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> entry.getKey().isCanOperateCredit())
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingCreditEmployee.isEmpty()) continue;

                Employee highestHoursCreditEmployee = highestHoursWorkingCreditEmployee.get();
                Employee lowestHoursCreditEmployee = lowestHoursNotWorkingCreditEmployee.get();
                Shift shift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursCreditEmployee);
                BigDecimal highestHours = context.getEmployeeHours().getOrDefault(highestHoursCreditEmployee, BigDecimal.ZERO);
                BigDecimal lowestHours = context.getEmployeeHours().getOrDefault(lowestHoursCreditEmployee, BigDecimal.ZERO);

                if (lowestEmployeeHasMoreHoursThanHighest(highestHours, lowestHours)) continue;
                if (context.getShiftLength(shift).compareTo(highestHours.subtract(lowestHours)) >= 0) continue;

                executeSwap(context, date, highestHoursCreditEmployee, lowestHoursCreditEmployee, shift);
                context.deleteEmployeeFromCredit(date, highestHoursCreditEmployee);
                context.assignEmployeeToCredit(date, lowestHoursCreditEmployee, shift);
                anySwapDone = true;
            }
        }
        return anySwapDone;
    }

    private boolean swapCheckoutShifts(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year, month);
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (isNotWorkingDay(context, date)) continue;

                Optional<Employee> highestHoursWorkingCheckoutEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> context.isEmployeeWorkingOnCheckout(entry.getKey(), date))
                        .filter(entry -> entry.getKey().isCanOperateCheckout())
                        .filter(entry -> !entry.getKey().isWarehouseman())
                        .filter(entry -> !entry.getKey().isCashier())
                        .filter(entry -> !context.isEmployeeWorkingInWarehouse(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeWorkingOnCredit(entry.getKey(), date))
                        .filter(entry -> !context.isOpeningOrClosingStore(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalShift(entry.getKey(), date))
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue().reversed())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (highestHoursWorkingCheckoutEmployee.isEmpty()) continue;

                Optional<Employee> lowestHoursNotWorkingCheckoutEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> entry.getKey().isCanOperateCheckout())
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingCheckoutEmployee.isEmpty()) continue;

                Employee highestHoursCheckoutEmployee = highestHoursWorkingCheckoutEmployee.get();
                Employee lowestHoursCheckoutEmployee = lowestHoursNotWorkingCheckoutEmployee.get();
                Shift shift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursCheckoutEmployee);
                BigDecimal highestHours = context.getEmployeeHours().getOrDefault(highestHoursCheckoutEmployee, BigDecimal.ZERO);
                BigDecimal lowestHours = context.getEmployeeHours().getOrDefault(lowestHoursCheckoutEmployee, BigDecimal.ZERO);

                if (lowestEmployeeHasMoreHoursThanHighest(highestHours, lowestHours)) continue;
                if (context.getShiftLength(shift).compareTo(highestHours.subtract(lowestHours)) >= 0) continue;

                executeSwap(context, date, highestHoursCheckoutEmployee, lowestHoursCheckoutEmployee, shift);
                context.deleteEmployeeFromCheckout(date, highestHoursCheckoutEmployee);
                context.assignEmployeeToCheckout(date, lowestHoursCheckoutEmployee, shift);
                anySwapDone = true;
            }
        }
        return anySwapDone;
    }

    private boolean swapOthersShifts(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year, month);
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (isNotWorkingDay(context, date)) continue;

                Optional<Employee> highestHoursWorkingEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> !entry.getKey().isWarehouseman())
                        .filter(entry -> !entry.getKey().isCashier())
                        .filter(entry -> !context.isEmployeeWorkingInWarehouse(entry.getKey(), date))
                        .filter(entry -> !context.isOpeningOrClosingStore(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeWorkingOnCredit(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeWorkingOnCheckout(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalShift(entry.getKey(), date))
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue().reversed())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (highestHoursWorkingEmployee.isEmpty()) continue;

                Optional<Employee> lowestHoursNotWorkingEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnDelegation(entry.getKey(), date))
                        .filter(entry -> !entry.getKey().isWarehouseman())
                        .filter(entry -> !entry.getKey().isCashier())
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingEmployee.isEmpty()) continue;

                Employee highestHoursEmployee = highestHoursWorkingEmployee.get();
                Employee lowestHoursEmployee = lowestHoursNotWorkingEmployee.get();
                Shift shift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursEmployee);
                BigDecimal highestHours = context.getEmployeeHours().getOrDefault(highestHoursEmployee, BigDecimal.ZERO);
                BigDecimal lowestHours = context.getEmployeeHours().getOrDefault(lowestHoursEmployee, BigDecimal.ZERO);

                if (lowestEmployeeHasMoreHoursThanHighest(highestHours, lowestHours)) continue;
                if (context.getShiftLength(shift).compareTo(highestHours.subtract(lowestHours)) >= 0) continue;

                executeSwap(context, date, highestHoursEmployee, lowestHoursEmployee, shift);
                anySwapDone = true;
            }
        }
        return anySwapDone;
    }
    
    private void executeSwap(ScheduleGeneratorContext context, LocalDate date, Employee from, Employee to, Shift shift) {
        log.info("{} USUWAM ZMIANE {}-{} PRACOWNIKA {} (SUMA GODZIN: {}) I DODAJE PRACOWNIKOWI {} (SUMA GODZIN: {})",
                date,
                shift.getStartHour(),
                shift.getEndHour(),
                from.getLastName(),
                context.getEmployeeHours().getOrDefault(from, BigDecimal.ZERO),
                to.getLastName(),
                context.getEmployeeHours().getOrDefault(to, BigDecimal.ZERO)
        );
        context.updateShiftOnSchedule(date, from, context.getDefaultDaysOffShift());
        context.registerShiftOnSchedule(date, to, shift, date.getDayOfWeek());
    }

    private boolean isNotWorkingDay(ScheduleGeneratorContext context, LocalDate date) {
        return Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum() < 1
                || date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private BigDecimal lowestNonSpecialistHours(ScheduleGeneratorContext context) {
        return context.getEmployeeHours().entrySet().stream()
                .filter(e -> !e.getKey().isWarehouseman())
                .filter(e -> !e.getKey().isCashier())
                .map(Map.Entry::getValue)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal highestNonSpecialistHours(ScheduleGeneratorContext context) {
        return context.getEmployeeHours().entrySet().stream()
                .filter(e -> !e.getKey().isWarehouseman())
                .filter(e -> !e.getKey().isCashier())
                .map(Map.Entry::getValue)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private static boolean lowestEmployeeHasMoreHoursThanHighest(BigDecimal highestHours, BigDecimal lowestHours) {
        return highestHours.compareTo(lowestHours) <= 0;
    }
}