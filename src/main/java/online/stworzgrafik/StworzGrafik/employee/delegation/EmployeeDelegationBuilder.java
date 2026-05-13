package online.stworzgrafik.StworzGrafik.employee.delegation;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

class EmployeeDelegationBuilder {
    public EmployeeDelegation createEmployeeDelegation(
            Store store,
            Employee employee,
            Integer year,
            Integer month,
            int[] monthlyDelegation) {
        return EmployeeDelegation.builder()
                .store(store)
                .employee(employee)
                .year(year)
                .month(month)
                .monthlyDelegation(monthlyDelegation)
                .build();
    }
}
