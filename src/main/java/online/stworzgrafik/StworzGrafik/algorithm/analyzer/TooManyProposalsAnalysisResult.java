package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.util.List;

record TooManyProposalsAnalysisResult(
        List<Employee> availableEmployees,
        List<Shift> shifts,
        int[] dailyDraft,
        int[] proposalsCount
) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        for (int indexHour = 0; indexHour < dailyDraft.length; indexHour++) {
            if (dailyDraft[indexHour] < proposalsCount[indexHour]) return true;
        }

        return shifts.size() > availableEmployees.size();
    }
}
