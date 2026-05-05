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
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
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

            boolean resolved = swapShifts(context);

            if (resolved) break;
        }
    }

    private boolean swapShifts(ScheduleGeneratorContext context){
        boolean anySwapDone = false;
        int timesToRepeat = 3;

        Integer year = context.getYear();
        Integer month = context.getMonth();
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(year, month);

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
                        .filter(entry -> !context.isEmployeeOpenClose(entry.getKey(), date))
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

                if (highestHoursEmployeeMonthlyHours.compareTo(lowestHoursEmployeeMonthlyHours) <= 0) {
                    log.info("Pominięto zamianę - {} ma mniej godzin ({}) niż {} ({})",
                            highestHoursEmployee.getLastName(), highestHoursEmployeeMonthlyHours,
                            lowestHoursEmployee.getLastName(), lowestHoursEmployeeMonthlyHours);
                    continue;
                }

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

                    anySwapDone = true;
                }
            } //to bedzie dobre jako ostatni krok, dla wszystkich (trzeba dodac dla managerow, ratalnych, kasjerow, potem to)
            //chodzi o to ze bedziemy podmieniali role ten co przychodzi za drugiego bierz jego role np openClose
        }
        return anySwapDone;
    }
}
