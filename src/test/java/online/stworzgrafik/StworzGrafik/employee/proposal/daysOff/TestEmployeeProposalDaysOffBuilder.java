package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.persistence.criteria.CriteriaBuilder;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

public class TestEmployeeProposalDaysOffBuilder {
    private Store store = new TestStoreBuilder().build();
    private Employee employee = new TestEmployeeBuilder().withStore(store).build();
    private Integer year = 2025;
    private Integer month = 12;
    private int[] monthlyDaysOff =  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};

    public TestEmployeeProposalDaysOffBuilder withStore(Store store){
        this.store = store;
        return this;
    }
    
    public EmployeeProposalDaysOff build(){
        return new EmployeeProposalDaysOffBuilder().createEmployeeProposalDaysOff(
                store,
                employee,
                year,
                month,
                monthlyDaysOff
        );
    }
}
