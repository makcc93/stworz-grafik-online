package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.util.List;

public record ClosingHourAnalysisResult(
        int closeHour,
        int closeHourDemandDraftValue,
        int closeHourProposalsCount,
        int proposalEmployeesCanCloseStoreCount,
        List<Employee> employeesWithCloseStoreProposals) implements ScheduleAnalysisResult{
    public boolean hasProblem(){
        return closeHourDemandDraftValue <= closeHourProposalsCount && proposalEmployeesCanCloseStoreCount < 1;
    }
}
