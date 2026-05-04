package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import java.math.BigDecimal;

public record ShiftSwapperAnalysisResult(
        BigDecimal employeeLowestValueOfWorkingHours,
        BigDecimal employeeHighestValueOfWorkingHours,
        BigDecimal minHoursDifference
) implements ScheduleAnalysisResult{
    public boolean hasProblem() {
        return employeeHighestValueOfWorkingHours.subtract(employeeLowestValueOfWorkingHours).compareTo(minHoursDifference) >= 0;
    }
}
