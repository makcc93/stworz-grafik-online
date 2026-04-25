package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.util.List;

record ManagerOpeningHourAnalysisResult(
    int openHour,
    int openHourDemandDraftValue,
    int openHourProposalsCount,
    int proposalEmployeesCanOpenStoreCount,
    List<Employee> employeesWithOpenStoreProposals,
    List<Shift> shifts) implements ScheduleAnalysisResult{

        public boolean hasProblem(){
            return openHourDemandDraftValue <= openHourProposalsCount && proposalEmployeesCanOpenStoreCount < 1;
        }
    }
