package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.util.List;

public record TooManyDayOffProposalResult(List<Employee> availableEmployees, List<Shift> shifts) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        return shifts.size() > availableEmployees.size();
    }
}