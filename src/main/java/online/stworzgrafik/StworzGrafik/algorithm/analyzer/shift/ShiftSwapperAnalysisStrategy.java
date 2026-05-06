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
public class ShiftSwapperAnalysisStrategy implements ScheduleAnalysisStrategy{
    private final CalendarCalculation calendarCalculation;
    private final int timesToRepeat = 3;
    private boolean anySwapDone = false;

    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.SHIFT_SWAPPER;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        BigDecimal minHoursDifference = BigDecimal.valueOf(8);

        BigDecimal employeeLowestValueOfWorkingHours = employees.stream()
                .filter(empl -> !empl.isWarehouseman())
                .sorted(Comparator.comparing(
                        e -> context.getEmployeeHours().getOrDefault(e, BigDecimal.ZERO)
                ))
                .findFirst()
                .map(e -> context.getEmployeeHours().getOrDefault(e, BigDecimal.ZERO))
                .orElse(BigDecimal.ZERO);

        BigDecimal employeeHighestValueOfWorkingHours = employees.stream()
                .filter(empl -> !empl.isWarehouseman())
                .sorted(Comparator.comparing(
                        e -> context.getEmployeeHours().getOrDefault(e, BigDecimal.ZERO)
                ).reversed())
                .findFirst()
                .map(e -> context.getEmployeeHours().getOrDefault(e, BigDecimal.ZERO))
                .orElse(BigDecimal.ZERO);

        return new ShiftSwapperAnalysisResult(employeeLowestValueOfWorkingHours,employeeHighestValueOfWorkingHours,minHoursDifference);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((ShiftSwapperAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate date) {
        BigDecimal minHoursDifference = ((ShiftSwapperAnalysisResult) result).minHoursDifference();

        while (true) {
            BigDecimal employeeLowestValueOfWorkingHours = context.getEmployeeHours().entrySet().stream()
                    .filter(entry -> !entry.getKey().isWarehouseman())
                    .filter(entry -> !entry.getKey().isCashier())
                    .sorted(Comparator.comparing(Map.Entry::getValue))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(BigDecimal.ZERO);

            BigDecimal employeeHighestValueOfWorkingHours = context.getEmployeeHours().entrySet().stream()
                    .filter(entry -> !entry.getKey().isWarehouseman())
                    .filter(entry -> !entry.getKey().isCashier())
                    .sorted(Comparator.comparing(
                            (Map.Entry<Employee, BigDecimal> entry) -> entry.getValue()
                    ).reversed())
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(BigDecimal.ZERO);

            if ((employeeHighestValueOfWorkingHours.subtract(employeeLowestValueOfWorkingHours)).compareTo(minHoursDifference) <= 0)
                break;
            boolean resolved = swapManagerShifts(context);
            resolved |= swapCreditShifts(context);
            resolved |= swapCheckoutShifts(context);
            resolved |= swapOthersShifts(context);

            if (!resolved) break;
        }
    }

    private boolean swapManagerShifts(ScheduleGeneratorContext context){
        log.info("                              MANAGERS");
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year,month);

        YearMonth yearMonth = YearMonth.of(year, month);
        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum() < 1 ||
                        date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

                Optional<Employee> highestHoursWorkingManager = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
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

                if (highestHoursWorkingManager.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }



                Optional<Employee> lowestHoursNotWorkingManager = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> entry.getKey().isCanOpenCloseStore())
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingManager.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }

                Employee highestHoursManager = highestHoursWorkingManager.get();
                Shift highestHoursManagerShift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursManager);
                BigDecimal highestHoursManagerMonthlyHours = context.getEmployeeHours().getOrDefault(highestHoursManager, BigDecimal.ZERO);

                Employee lowestHoursManager = lowestHoursNotWorkingManager.get();
                BigDecimal lowestHoursManagerMonthlyHours = context.getEmployeeHours().getOrDefault(lowestHoursManager, BigDecimal.ZERO);

                BigDecimal managersHoursDifference = highestHoursManagerMonthlyHours.subtract(lowestHoursManagerMonthlyHours);

                if (lowestEmployeeHasMoreHoursThanHighest(highestHoursManagerMonthlyHours, lowestHoursManagerMonthlyHours, highestHoursManager, lowestHoursManager))
                    continue;

                if (context.getShiftLength(highestHoursManagerShift).compareTo(managersHoursDifference) < 0) {
                    log.info("          {} USUWAM ZMIANE {}-{} PRACOWNIKA {} (SUMA GODZIN: {}) I DODAJE PRACOWNIKOWI {} (SUMA GODZIN: {})",
                            date,
                            highestHoursManagerShift.getStartHour(),
                            highestHoursManagerShift.getEndHour(),
                            highestHoursManager.getLastName(),
                            highestHoursManagerMonthlyHours,
                            lowestHoursManager.getLastName(),
                            lowestHoursManagerMonthlyHours
                    );
                    context.updateShiftOnSchedule(date, highestHoursManager, context.getDefaultDaysOffShift());
                    context.registerShiftOnSchedule(date, lowestHoursManager, highestHoursManagerShift, date.getDayOfWeek());

                    context.deleteEmployeeToOpenClose(date, highestHoursManager);
                    context.assignEmployeeToOpenClose(date, lowestHoursManager, highestHoursManagerShift);

                    anySwapDone = true;
                }
            }
        }

        return anySwapDone;
    }

    private boolean swapCreditShifts(ScheduleGeneratorContext context) {
        log.info("                              CREDITS");
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year,month);

        YearMonth yearMonth = YearMonth.of(year, month);
        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum() < 1 ||
                        date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

                Optional<Employee> highestHoursWorkingCreditEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
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

                if (highestHoursWorkingCreditEmployee.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }

                Optional<Employee> lowestHoursNotWorkingCreditEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> entry.getKey().isCanOperateCredit())
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingCreditEmployee.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }

                Employee highestHoursCreditEmployee = highestHoursWorkingCreditEmployee.get();
                Shift highestHoursCreditEmployeeShift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursCreditEmployee);
                BigDecimal highestHoursCreditEmployeeMonthlyHoursCount = context.getEmployeeHours().getOrDefault(highestHoursCreditEmployee, BigDecimal.ZERO);

                Employee lowestHoursCreditEmployee = lowestHoursNotWorkingCreditEmployee.get();
                BigDecimal lowestHoursCreditEmployeeMonthlyHoursCount = context.getEmployeeHours().getOrDefault(lowestHoursCreditEmployee, BigDecimal.ZERO);

                BigDecimal creditEmployeesHoursDifference = highestHoursCreditEmployeeMonthlyHoursCount.subtract(lowestHoursCreditEmployeeMonthlyHoursCount);
                if (lowestEmployeeHasMoreHoursThanHighest(highestHoursCreditEmployeeMonthlyHoursCount, lowestHoursCreditEmployeeMonthlyHoursCount, highestHoursCreditEmployee, lowestHoursCreditEmployee))
                    continue;

                if (context.getShiftLength(highestHoursCreditEmployeeShift).compareTo(creditEmployeesHoursDifference) < 0) {
                    log.info("          {} USUWAM ZMIANE {}-{} PRACOWNIKA {} (SUMA GODZIN: {}) I DODAJE PRACOWNIKOWI {} (SUMA GODZIN: {})",
                            date,
                            highestHoursCreditEmployeeShift.getStartHour(),
                            highestHoursCreditEmployeeShift.getEndHour(),
                            highestHoursCreditEmployee.getLastName(),
                            highestHoursCreditEmployeeMonthlyHoursCount,
                            lowestHoursCreditEmployee.getLastName(),
                            lowestHoursCreditEmployeeMonthlyHoursCount
                    );
                    context.updateShiftOnSchedule(date, highestHoursCreditEmployee, context.getDefaultDaysOffShift());
                    context.registerShiftOnSchedule(date, lowestHoursCreditEmployee, highestHoursCreditEmployeeShift, date.getDayOfWeek());

                    context.deleteEmployeeFromCredit(date,highestHoursCreditEmployee);
                    context.assignEmployeeToCredit(date,lowestHoursCreditEmployee,highestHoursCreditEmployeeShift);
                }
            }
        }
        return anySwapDone;
    }

    private boolean swapCheckoutShifts(ScheduleGeneratorContext context) {
        log.info("                              CHECKOUTS");

        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum() < 1 ||
                        date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

                Optional<Employee> highestHoursWorkingCheckoutEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
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

                if (highestHoursWorkingCheckoutEmployee.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }

                Optional<Employee> lowestHoursNotWorkingCheckoutEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> entry.getKey().isCanOperateCheckout())
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingCheckoutEmployee.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }

                Employee highestHoursCheckoutEmployee = highestHoursWorkingCheckoutEmployee.get();
                Shift highestHoursCheckoutEmployeeShift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursCheckoutEmployee);
                BigDecimal highestHoursCheckoutEmployeeMonthlyHoursCount = context.getEmployeeHours().getOrDefault(highestHoursCheckoutEmployee, BigDecimal.ZERO);

                Employee lowestHoursCheckoutEmployee = lowestHoursNotWorkingCheckoutEmployee.get();
                BigDecimal lowestHoursCheckoutEmployeeMonthlyHoursCount = context.getEmployeeHours().getOrDefault(lowestHoursCheckoutEmployee, BigDecimal.ZERO);

                BigDecimal checkoutEmployeesHoursDifference = highestHoursCheckoutEmployeeMonthlyHoursCount.subtract(lowestHoursCheckoutEmployeeMonthlyHoursCount);
                if (lowestEmployeeHasMoreHoursThanHighest(highestHoursCheckoutEmployeeMonthlyHoursCount, lowestHoursCheckoutEmployeeMonthlyHoursCount, highestHoursCheckoutEmployee, lowestHoursCheckoutEmployee))
                    continue;

                if (context.getShiftLength(highestHoursCheckoutEmployeeShift).compareTo(checkoutEmployeesHoursDifference) < 0) {
                    log.info("          {} USUWAM ZMIANE {}-{} PRACOWNIKA {} (SUMA GODZIN: {}) I DODAJE PRACOWNIKOWI {} (SUMA GODZIN: {})",
                            date,
                            highestHoursCheckoutEmployeeShift.getStartHour(),
                            highestHoursCheckoutEmployeeShift.getEndHour(),
                            highestHoursCheckoutEmployee.getLastName(),
                            highestHoursCheckoutEmployeeMonthlyHoursCount,
                            lowestHoursCheckoutEmployee.getLastName(),
                            lowestHoursCheckoutEmployeeMonthlyHoursCount
                    );
                    context.updateShiftOnSchedule(date, highestHoursCheckoutEmployee, context.getDefaultDaysOffShift());
                    context.registerShiftOnSchedule(date, lowestHoursCheckoutEmployee, highestHoursCheckoutEmployeeShift, date.getDayOfWeek());

                    context.deleteEmployeeFromCheckout(date,highestHoursCheckoutEmployee);
                    context.assignEmployeeToCheckout(date,lowestHoursCheckoutEmployee,highestHoursCheckoutEmployeeShift);
                }
            }
        }
        return anySwapDone;
    }


    private boolean swapOthersShifts(ScheduleGeneratorContext context){
        log.info("                              OTHERS");
        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year,month);

        YearMonth yearMonth = YearMonth.of(year, month);
        for (int repeat = 1; repeat <= timesToRepeat; repeat++) {
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date,new int[24])).sum() < 1 ||
                    date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

                Optional<Employee> highestHoursWorkingEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> context.employeeIsWorking(entry.getKey(), date))
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

                if (highestHoursWorkingEmployee.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }

                Employee highestHoursEmployee = highestHoursWorkingEmployee.get();


                Optional<Employee> lowestHoursNotWorkingEmployee = context.getEmployeeHours().entrySet().stream()
                        .filter(entry -> !context.employeeIsWorking(entry.getKey(), date))
                        .filter(entry -> !entry.getKey().isWarehouseman())
                        .filter(entry -> !entry.getKey().isCashier())
                        .filter(entry -> !context.isEmployeeOnRestRequirementDayOff(entry.getKey(), date))
                        .filter(entry -> !context.employeeIsOnVacation(entry.getKey(), date))
                        .filter(entry -> !context.employeeHasProposalDaysOff(entry.getKey(), date))
                        .filter(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0) < monthlyMaxWorkingDays)
                        .sorted(Map.Entry.<Employee, BigDecimal>comparingByValue())
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestHoursNotWorkingEmployee.isEmpty()) {
                    log.info("Nie znaleziono pracownika do zamiany pracy");
                    continue;
                }
                Employee lowestHoursEmployee = lowestHoursNotWorkingEmployee.get();

                Shift highestHoursEmployeeShift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(highestHoursEmployee);
                BigDecimal lowestHoursEmployeeMonthlyHours = context.getEmployeeHours().getOrDefault(lowestHoursEmployee, BigDecimal.ZERO);
                BigDecimal highestHoursEmployeeMonthlyHours = context.getEmployeeHours().getOrDefault(highestHoursEmployee, BigDecimal.ZERO);
                BigDecimal employeesHoursDifference = highestHoursEmployeeMonthlyHours.subtract(lowestHoursEmployeeMonthlyHours);

                if (lowestEmployeeHasMoreHoursThanHighest(highestHoursEmployeeMonthlyHours, lowestHoursEmployeeMonthlyHours, highestHoursEmployee, lowestHoursEmployee))
                    continue;
                if (context.getShiftLength(highestHoursEmployeeShift).compareTo(employeesHoursDifference) < 0) {
                    log.info("          {} USUWAM ZMIANE {}-{} PRACOWNIKA {} (SUMA GODZIN: {}) I DODAJE PRACOWNIKOWI {} (SUMA GODZIN: {})",
                            date,
                            highestHoursEmployeeShift.getStartHour(),
                            highestHoursEmployeeShift.getEndHour(),
                            highestHoursEmployee.getLastName(),
                            highestHoursEmployeeMonthlyHours,
                            lowestHoursEmployee.getLastName(),
                            lowestHoursEmployeeMonthlyHours
                    );
                    context.updateShiftOnSchedule(date, highestHoursEmployee, context.getDefaultDaysOffShift());
                    context.registerShiftOnSchedule(date, lowestHoursEmployee, highestHoursEmployeeShift, date.getDayOfWeek());
                }
            }

        }
        return anySwapDone;
    }

    private static boolean lowestEmployeeHasMoreHoursThanHighest(BigDecimal highestHoursManagerMonthlyHours, BigDecimal lowestHoursManagerMonthlyHours, Employee highestHoursManager, Employee lowestHoursManager) {
        if (highestHoursManagerMonthlyHours.compareTo(lowestHoursManagerMonthlyHours) <= 0) {
            log.info("Pominięto zamianę - {} ma mniej godzin ({}) niż {} ({})",
                    highestHoursManager.getLastName(), highestHoursManagerMonthlyHours,
                    lowestHoursManager.getLastName(), lowestHoursManagerMonthlyHours);
            return true;
        }
        return false;
    }
}
