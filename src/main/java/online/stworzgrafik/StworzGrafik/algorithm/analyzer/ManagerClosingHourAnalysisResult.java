package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.util.List;

public record ManagerClosingHourAnalysisResult(
        int closeHour,
        int closeHourDemandDraftValue,
        int closeHourProposalsCount,
        int proposalEmployeesCanCloseStoreCount,
        List<Employee> employeesWithCloseStoreProposals,
        List<Shift> shifts) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        return closeHourDemandDraftValue <= closeHourProposalsCount && proposalEmployeesCanCloseStoreCount < 1;
    }
}
