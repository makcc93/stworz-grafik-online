package online.stworzgrafik.StworzGrafik.employee.vacation;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
class EmployeeVacationBuilder {
    public EmployeeVacation createEmployeeVacation(
            Store store,
            Employee employee,
            Integer year,
            Integer month,
            int[] monthlyVacation) {
        return EmployeeVacation.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(month)
                .monthlyVacation(monthlyVacation)
                .build();
    }
}