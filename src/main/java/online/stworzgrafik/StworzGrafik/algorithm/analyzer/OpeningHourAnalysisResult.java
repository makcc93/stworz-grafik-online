package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.util.List;

record OpeningHourAnalysisResult (
    int openHour,
    int openHourDemandDraftValue,
    int openHourProposalsCount,
    int proposalEmployeesCanOpenStoreCount,
    List<Employee> employeesWithOpenStoreProposals) implements ScheduleAnalysisResult{
        public boolean hasProblem(){
            return openHourDemandDraftValue <= openHourProposalsCount && proposalEmployeesCanOpenStoreCount < 1;
        }
    }
