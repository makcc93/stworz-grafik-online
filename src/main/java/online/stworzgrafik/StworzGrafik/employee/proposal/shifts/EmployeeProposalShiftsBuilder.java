package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
class EmployeeProposalShiftsBuilder {
    public EmployeeProposalShifts createEmployeeProposalShifts(
            Store store,
            Employee employee,
            LocalDate date,
            int[] dailyProposalShift) {
        return EmployeeProposalShifts.builder()
                .store(store)
                .employee(employee)
                .date(date)
                .dailyProposalShift(dailyProposalShift)
                .build();
    }
}
