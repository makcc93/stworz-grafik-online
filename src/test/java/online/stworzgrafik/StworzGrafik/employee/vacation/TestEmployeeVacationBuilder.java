package online.stworzgrafik.StworzGrafik.employee.vacation;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

public class TestEmployeeVacationBuilder {
    private Store store = new TestStoreBuilder().build();
    private Employee employee = new TestEmployeeBuilder().withStore(store).build();
    private Integer year = 2022;
    private Integer month = 1;
    private int[] monthlyVacation =  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    public TestEmployeeVacationBuilder withStore(Store store){
        this.store = store;
        return this;
    }

    public TestEmployeeVacationBuilder withEmployee(Employee employee){
        this.employee = employee;
        return this;
    }

    public TestEmployeeVacationBuilder withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestEmployeeVacationBuilder withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestEmployeeVacationBuilder withMonthlyVacation(int[] monthlyVacation){
        this.monthlyVacation = monthlyVacation;
        return this;
    }

    public EmployeeVacation build(){
        return new EmployeeVacationBuilder().createEmployeeVacation(
                store,
                employee,
                year,
                month,
                monthlyVacation
        );
    }
}