package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public record UnbalancedShiftDistributionResult(Map<Employee, Set<LocalDate>> daysOffInARow) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        return !daysOffInARow.isEmpty();
    }
}
