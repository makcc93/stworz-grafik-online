package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.algorithm.EmployeeToShiftMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScheduleAnalyzer {

    private Map<AnalyzeType, ScheduleAnalysisStrategy> strategies;

    public ScheduleAnalyzer(List<ScheduleAnalysisStrategy> strategies){
        this.strategies = strategies.stream()
                .collect(
                        Collectors.toMap(
                                ScheduleAnalysisStrategy::getSupportedType,
                                s -> s
                        )
                );
    }

    public void analyzeAndResolve(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees, AnalyzeType type){
        ScheduleAnalysisStrategy scheduleAnalysisStrategy = strategies.get(type);

        ScheduleAnalysisResult result = scheduleAnalysisStrategy.analyze(context, day,shifts,availableEmployees);

        if (scheduleAnalysisStrategy.hasProblem(result)){
            scheduleAnalysisStrategy.resolve(result,context,day);
        }
    }
}
