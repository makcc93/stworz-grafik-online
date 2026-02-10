package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

public class TestScheduleBuilder {
    private Region region = new TestRegionBuilder().build();
    private Branch branch = new TestBranchBuilder().withRegion(region).build();
    private Store store = new TestStoreBuilder().withBranch(branch).build();
    private Integer year = 2020;
    private Integer month = 10;
    private Long createByUserId = 123L;

    public TestScheduleBuilder withRegion(Region region){
        this.region = region;
        return this;
    }

    public TestScheduleBuilder withBranch(Branch branch){
        this.branch = branch;
        return this;
    }

    public TestScheduleBuilder withStore(Store store){
        this.store = store;
        return this;
    }

    public TestScheduleBuilder withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestScheduleBuilder withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestScheduleBuilder withCreateByUserId(Long createByUserId){
        this.createByUserId = createByUserId;
        return this;
    }

    public Schedule build(){
        return new ScheduleBuilder().createSchedule(
                store,
                year,
                month,
                createByUserId
        );
    }
}
