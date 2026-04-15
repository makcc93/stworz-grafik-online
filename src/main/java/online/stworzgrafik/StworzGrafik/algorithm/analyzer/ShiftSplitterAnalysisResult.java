package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.util.Map;

public record ShiftSplitterAnalysisResult(
        Map<Employee, Integer> workingDaysCount,
        int monthlyMaxWorkingDays
) {
}
