package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ShiftSwapperAnalysisStrategy implements ScheduleAnalysisStrategy{
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

        //dalej trzeba wymyslec jak przechodzic przez pracownikow i szukac dni ktore sa wolne (bez dodatkowych obostrzen i dodawac zmiane osobie z mala liczba godzin)
    }
}
