package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

public record HoursSwapperAnalysisResult(
        int employeeLowestValueOfWorkingHours,
        int employeeHighestValueOfWorkingHours,
        int maxHoursDifference
) implements ScheduleAnalysisResult{
    public boolean hasProblem() {
        return (employeeHighestValueOfWorkingHours - employeeLowestValueOfWorkingHours) > maxHoursDifference;
    }
}
