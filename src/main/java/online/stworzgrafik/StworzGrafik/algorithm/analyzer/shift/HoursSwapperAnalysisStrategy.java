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

            if ((employeeHighestValueOfWorkingHours.subtract(employeeLowestValueOfWorkingHours)).compareTo(maxHoursDifference) <= 0) break;

            boolean resolved = swapHours(context);

            if (resolved) break;
        }
    }

    private boolean swapHours(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        List<Employee> employees = context.getStoreActiveEmployees();
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

                Employee highestHoursEmployee = employeeHours.entrySet().stream()
                        .sorted(Comparator.comparing(
                                (Map.Entry<Employee, BigDecimal> entry) -> entry.getValue()
                        ).reversed())
                        .map(Map.Entry::getKey)
                        .toList()
                        .getFirst();

                BigDecimal highestEmployeeHoursCount = employeeHours.getOrDefault(highestHoursEmployee, BigDecimal.ZERO);
                Shift highestHoursEmployeeShift = employeeShift.getOrDefault(highestHoursEmployee, context.getDefaultDaysOffShift());
                BigDecimal highestHoursEmployeeShiftLength = BigDecimal.valueOf(getShiftLength(highestHoursEmployeeShift));

                Employee lowestHoursEmployee = employeeHours.entrySet().stream()
                        .sorted(Comparator.comparing(
                                Map.Entry::getValue
                        ))
                        .map(Map.Entry::getKey)
                        .toList()
                        .getFirst();

                BigDecimal lowestEmployeeHoursCount = employeeHours.getOrDefault(lowestHoursEmployee, BigDecimal.ZERO);
                Shift lowestHoursEmployeeShift = employeeShift.getOrDefault(lowestHoursEmployee, context.getDefaultDaysOffShift());
                BigDecimal lowestHoursEmployeeShiftLength = BigDecimal.valueOf(getShiftLength(lowestHoursEmployeeShift));

                if ((highestHoursEmployeeShiftLength.compareTo(lowestHoursEmployeeShiftLength) > 0) &&
                        (highestEmployeeHoursCount.subtract(lowestEmployeeHoursCount)).compareTo(highestHoursEmployeeShiftLength.subtract(lowestHoursEmployeeShiftLength)) > 0) {
                    context.updateShiftOnSchedule(date, highestHoursEmployee, lowestHoursEmployeeShift);
                    context.updateShiftOnSchedule(date, lowestHoursEmployee, highestHoursEmployeeShift);
                    anySwapDone = true;
                }
            }
        }
        return anySwapDone;
    }

    private int getShiftLength(Shift shift){
        return shift.getEndHour().getHour() - shift.getStartHour().getHour();
    }
}