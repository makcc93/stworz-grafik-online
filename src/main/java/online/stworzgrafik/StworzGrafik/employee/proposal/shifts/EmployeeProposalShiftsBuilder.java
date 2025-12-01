package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
class EmployeeProposalShiftsBuilder {
    public EmployeeProposalShifts createEmployeeProposalShifts(
            Store store,
            Employee employee,
            Integer year,
            Integer month,
            Integer day,
            int[] dailyProposalShift) {
        return EmployeeProposalShifts.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(month)
                .day(day)
                .dailyProposalShift(dailyProposalShift)
                .build();
    }
}
