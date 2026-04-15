package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftSplitterAnalysisStrategy implements ScheduleAnalysisStrategy {
    private final CalendarCalculation calendarCalculation;
    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.SHIFT_SPLITTER;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(),context.getMonth());
        LinkedHashMap<Employee,Integer> workingDaysCount = new LinkedHashMap<>();

        for (Employee employee : employees) {
            Integer value = context.getWorkingDaysCount().getOrDefault(employee, 0);

            workingDaysCount.put(employee,value);
        }

        LinkedHashMap<Employee, Integer> filteredWorkingDaysCountSortedDesc = workingDaysCount.entrySet().stream()
                .filter(entry -> entry.getKey().isWarehouseman())
                .filter(entry -> entry.getKey().isCashier())
                .sorted((k1, k2) -> k2.getValue().compareTo(k1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        Integer highestValue = filteredWorkingDaysCountSortedDesc.entrySet().stream().findFirst().get().getValue();

        LinkedHashMap<Employee, Integer> filteredWorkingDaysCountSortedAsc = workingDaysCount.entrySet().stream()
                .filter(entry -> entry.getKey().isWarehouseman())
                .filter(entry -> entry.getKey().isCashier())
                .sorted((k1, k2) -> k1.getValue().compareTo(k2.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        Integer lowestValue = filteredWorkingDaysCountSortedDesc.entrySet().stream().findFirst().get().getValue();

        if (highestValue - lowestValue > 2 ||  monthlyMaxWorkingDays - highestValue > 2){
            //problem to solve
        }
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return false;
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        while (context.getWorkingDaysCount())
    }
}
