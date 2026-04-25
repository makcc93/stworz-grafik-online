package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

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
        int maxHoursDifference = 3;

        int employeeLowestValueOfWorkingHours = employees.stream()
                .filter(empl -> !empl.isWarehouseman())
                .sorted(Comparator.comparingInt(
                        e -> context.getEmployeeHours().getOrDefault(e, 0)
                ))
                .findFirst()
                .map(e -> context.getEmployeeHours().getOrDefault(e, 0))
                .orElse(0);

        int employeeHighestValueOfWorkingHours = employees.stream()
                .filter(empl -> !empl.isWarehouseman())
                .sorted(Comparator.comparingInt(
                        e -> context.getEmployeeHours().getOrDefault(e, 0)
                ).reversed())
                .findFirst()
                .map(e -> context.getEmployeeHours().getOrDefault(e, 0))
                .orElse(0);

        return new HoursSwapperAnalysisResult(employeeLowestValueOfWorkingHours,employeeHighestValueOfWorkingHours,maxHoursDifference);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((HoursSwapperAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        int maxHoursDifference = ((HoursSwapperAnalysisResult) result).maxHoursDifference();

        while (true) {
            int employeeLowestValueOfWorkingHours = context.getEmployeeHours().entrySet().stream()
                    .filter(entry -> entry.getKey().isCanOpenCloseStore()) //for test
                    .filter(entry -> !entry.getKey().isWarehouseman())
                    .filter(entry -> !entry.getKey().isCashier())
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(0);

            int employeeHighestValueOfWorkingHours = context.getEmployeeHours().entrySet().stream()
                    .filter(entry -> entry.getKey().isCanOpenCloseStore()) //for test
                    .filter(entry -> !entry.getKey().isWarehouseman())
                    .filter(entry -> !entry.getKey().isCashier())
                    .sorted(Comparator.comparingInt(
                            (Map.Entry<Employee, Integer> entry) -> entry.getValue()
                    ).reversed())
                    .findFirst()
                    .map(Map.Entry::getValue)
                    .orElse(0);

            if ((employeeHighestValueOfWorkingHours - employeeLowestValueOfWorkingHours) <= maxHoursDifference) break;

            boolean resolved = swapHoursOnManagers(context);

            resolved |= swapHoursOnCreditEmployees(context);
            resolved |= swapHoursOnOthers(context);

            if (resolved) break;
        }
    }

    private boolean swapHoursOnManagers(ScheduleGeneratorContext context){
        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(Employee::isCanOpenCloseStore)
                .toList();

        return swapHours(context, employees);
    }

    private boolean swapHoursOnCreditEmployees(ScheduleGeneratorContext context){
        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(empl -> !empl.isCanOpenCloseStore())
                .filter(empl -> !empl.isCashier())
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !empl.isPok())
                .filter(Employee::isCanOperateCredit)
                .toList();

        return swapHours(context, employees);
    }

    private boolean swapHoursOnOthers(ScheduleGeneratorContext context){
        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(empl -> !empl.isCanOpenCloseStore())
                .filter(empl -> !empl.isCashier())
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !empl.isPok())
                .filter(empl -> !empl.isCanOperateCredit())
                .toList();

        return swapHours(context, employees);
    }

    private boolean swapHours(ScheduleGeneratorContext context, List<Employee> employees) {
        boolean anySwapDone = false;
        int timesRepeat = 10;

        YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

        for (int repeat = 1; repeat <= timesRepeat; repeat++){
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);

                if (holidayManager.isHoliday(date) || Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum() < 1)
                    continue;

                Map<Employee, Integer> employeeHours = new HashMap<>();
                Map<Employee, Shift> employeeShift = new HashMap<>();

                for (Employee employee : employees) {
                    if (context.employeeHasProposalShift(employee, date)) continue;
                    if (context.employeeHasProposalDaysOff(employee, date)) continue;
                    if (context.isEmployeeWorkingInWarehouse(employee, date)) continue;
                    if (context.employeeIsOnVacation(employee, day)) continue;
                    if (!context.employeeIsWorking(employee, date)) continue;

                    employeeHours.put(employee, context.getEmployeeHours().getOrDefault(employee, 0));

                    Shift shift = context.getFinalSchedule().getOrDefault(date, new HashMap<>()).getOrDefault(employee, context.getDefaultDaysOffShift());

                    employeeShift.put(employee, shift);
                }

                if (employeeShift.size() < 2) {
                    log.info("Brak 2 lub więcej praacowników. {}", date);
                    continue;
                }

                Employee highestHoursEmployee = employeeHours.entrySet().stream()
                        .sorted(Comparator.comparingInt(
                                (Map.Entry<Employee, Integer> entry) -> entry.getValue()
                        ).reversed())
                        .map(Map.Entry::getKey)
                        .toList()
                        .getFirst();

                Integer highestEmployeeHoursCount = employeeHours.getOrDefault(highestHoursEmployee, 0);
                Shift highestHoursEmployeeShift = employeeShift.getOrDefault(highestHoursEmployee, context.getDefaultDaysOffShift());
                int highestHoursEmployeeShiftLength = getShiftLength(highestHoursEmployeeShift);

                Employee lowestHoursEmployee = employeeHours.entrySet().stream()
                        .sorted(Comparator.comparingInt(
                                Map.Entry::getValue
                        ))
                        .map(Map.Entry::getKey)
                        .toList()
                        .getFirst();

                Integer lowestEmployeeHoursCount = employeeHours.getOrDefault(lowestHoursEmployee, 0);
                Shift lowestHoursEmployeeShift = employeeShift.getOrDefault(lowestHoursEmployee, context.getDefaultDaysOffShift());
                int lowestHoursEmployeeShiftLength = getShiftLength(lowestHoursEmployeeShift);

                if ((highestHoursEmployeeShiftLength > lowestHoursEmployeeShiftLength) &&
                        (highestEmployeeHoursCount - lowestEmployeeHoursCount) > (highestHoursEmployeeShiftLength - lowestHoursEmployeeShiftLength)) {
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