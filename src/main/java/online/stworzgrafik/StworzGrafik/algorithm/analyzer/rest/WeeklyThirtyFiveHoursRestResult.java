package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.time.LocalDate;
import java.util.List;

public record WeeklyThirtyFiveHoursRestResult(List<Violation> violations) implements RestAnalyzerResult {
    public record Violation(Employee employee, int weekIndex, LocalDate weekStart, LocalDate weekEnd, int maxFreeHoursInARow) {
    }
}