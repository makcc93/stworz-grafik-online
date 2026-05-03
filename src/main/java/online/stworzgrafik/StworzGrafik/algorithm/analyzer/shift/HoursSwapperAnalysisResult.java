package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import java.math.BigDecimal;

public record HoursSwapperAnalysisResult(
        BigDecimal employeeLowestValueOfWorkingHours,
        BigDecimal employeeHighestValueOfWorkingHours,
        BigDecimal maxHoursDifference
) implements ScheduleAnalysisResult{
    public boolean hasProblem() {
        return employeeHighestValueOfWorkingHours.subtract(employeeLowestValueOfWorkingHours).compareTo(maxHoursDifference) > 0;
    }
}
