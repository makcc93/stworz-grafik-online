package online.stworzgrafik.StworzGrafik.dataBuilderForTests.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreBuilder;

import java.time.LocalTime;

public class TestStoreBuilder {
    private String name = "TESTSTORE";
    private String storeCode = "AA";
    private String location = "TESTLOCATION";
    private Branch branch = new TestBranchBuilder().build();
    private LocalTime openHour = LocalTime.of(9,0);
    private LocalTime closeHour = LocalTime.of(20,0);

    public TestStoreBuilder withName(String name){
        this.name = name;
        return this;
    }

    public TestStoreBuilder withStoreCode(String storeCode){
        this.storeCode = storeCode;
        return this;
    }

    public TestStoreBuilder withLocation(String location){
        this.location = location;
        return this;
    }

    public TestStoreBuilder withBranch(Branch branch){
        this.branch = branch;
        return this;
    }

    public TestStoreBuilder withOpenHour(LocalTime openHour){
        this.openHour = openHour;
        return this;
    }

    public TestStoreBuilder withCloseHour(LocalTime closeHour){
        this.closeHour = closeHour;
        return this;
    }

    public Store build(){
        return new StoreBuilder().createStore(
                name,
                storeCode,
                location,
                branch,
                openHour,
                closeHour
        );
    }
}
