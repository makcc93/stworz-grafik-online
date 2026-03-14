package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenCloseHourProposalAnalyzer {

    public OpenCloseHourProposalResult analyze(ScheduleGeneratorContext context, LocalDate day, StoreHourType storeHourType){

        Map<LocalDate, int[]> originalStoreDraft = context.getUneditedOriginalDateStoreDraft();

        int targetHour = storeHourType == StoreHourType.OPENING ? findOpenStoreHour(originalStoreDraft,day)
                                                                : findCloseStoreHour(originalStoreDraft,day);

        int targetHourDemandDraftValue = originalStoreDraft.get(day)[targetHour];

        int[] arrayDailyProposalsCount = new int[24];
        int proposalEmployeesCanOpenCloseStoreCount = 0;
        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        List<Employee> employeesWithOpenCloseStoreProposals = new ArrayList<>();

        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposals.entrySet()){
            Employee employee = proposalEntry.getKey();

            if (employee.isCanOpenCloseStore()){
                proposalEmployeesCanOpenCloseStoreCount++;
            }

            int[] singleEmployeeProposal = proposalEntry.getValue();
            for (int i = 0; i < arrayDailyProposalsCount.length; i++){
                arrayDailyProposalsCount[i] += singleEmployeeProposal[i];
            }


            if (singleEmployeeProposal[targetHour] > 0){
                employeesWithOpenCloseStoreProposals.add(employee);
            }
        }

        int targetHourProposalsCount = arrayDailyProposalsCount[targetHour];

        return new OpenCloseHourProposalResult(targetHour,targetHourDemandDraftValue,targetHourProposalsCount,proposalEmployeesCanOpenCloseStoreCount,employeesWithOpenCloseStoreProposals);
    }

    private int findCloseStoreHour(Map<LocalDate, int[]> originalStoreDrafts, LocalDate day) {
        int targetHour = 23;

        for (int i = 23; i >= 0; i--){
            if (originalStoreDrafts.get(day)[i] > 0){
                targetHour = i;
                break;
            }
        }
        return targetHour;
    }

    private int findOpenStoreHour(Map<LocalDate, int[]> originalStoreDrafts, LocalDate day) {
        int targetHour = 0;
        for (int i = 0; i < originalStoreDrafts.get(day).length; i++){
            if (originalStoreDrafts.get(day)[i] > 0){
                targetHour = i;
                break;
            }
        }
        return targetHour;
    }
}
