package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
class EmployeeProposalDaysOffBuilder {
    public EmployeeProposalDaysOff createEmployeeProposalDaysOff(
            Store store,
            Employee employee,
            Integer year,
            Integer month,
            int[] monthlyDaysOff) {
        return EmployeeProposalDaysOff.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(month)
                .monthlyDaysOff(monthlyDaysOff)
                .build();
    }
}
