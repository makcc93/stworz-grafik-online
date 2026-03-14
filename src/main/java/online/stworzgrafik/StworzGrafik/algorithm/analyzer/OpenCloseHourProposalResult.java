package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.util.List;

public record OpenCloseHourProposalResult(
        int targetHour,
        int targetHourDemandDraftValue,
        int targetHourProposalsCount,
        int proposalEmployeesCanOpenCloseStoreCount,
        List<Employee> employeesWithOpenCloseStoreProposals
) {
    public boolean hasProblem(){
        return targetHourDemandDraftValue <= targetHourProposalsCount && proposalEmployeesCanOpenCloseStoreCount < 1;
    }
}
