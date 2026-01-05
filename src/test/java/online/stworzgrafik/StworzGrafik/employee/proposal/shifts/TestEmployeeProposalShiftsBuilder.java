package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestEmployeeProposalShiftsBuilder {
    private Long id = 100L;
    private Region region = new TestRegionBuilder().build();
    private Branch branch = new TestBranchBuilder().withRegion(region).build();
    private Store store = new TestStoreBuilder().withBranch(branch).build();
    private Employee employee = new TestEmployeeBuilder().withStore(store).build();
    private LocalDate date = LocalDate.of(2020,05,10);
    private int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0};
    private LocalDateTime createdAt = LocalDateTime.of(2022,12,12,12,12);
    private LocalDateTime updatedAt = LocalDateTime.now();

    public TestEmployeeProposalShiftsBuilder withId(Long id){
        this.id = id;
        return this;
    }

    public TestEmployeeProposalShiftsBuilder withStore(Store store){
        this.store = store;
        return this;
    }

    public TestEmployeeProposalShiftsBuilder withEmployee(Employee employee){
        this.employee = employee;
        return this;
    }

    public TestEmployeeProposalShiftsBuilder withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestEmployeeProposalShiftsBuilder withDailyProposalShift(int[] dailyProposalShift){
        this.dailyProposalShift = dailyProposalShift;
        return this;
    }

    public TestEmployeeProposalShiftsBuilder withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestEmployeeProposalShiftsBuilder withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public EmployeeProposalShifts build(){
        return new EmployeeProposalShifts(
                id,
                store,
                employee,
                date,
                dailyProposalShift,
                createdAt,
                updatedAt
        );
    }
}
