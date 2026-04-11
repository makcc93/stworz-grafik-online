package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.util.List;

public record UnderstaffedAnalysisResult(List<Employee> availableEmployees, List<Shift> shifts) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        return shifts.size() > availableEmployees.size();
    }
}
