package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ScheduleAnalysisStrategy {
    AnalyzeType getSupportedType();
    ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees);
    boolean hasProblem(ScheduleAnalysisResult result);
    void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day);
}
