package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;

public class TestStoreBuilder {
    private String name = "TESTSTORE";
    private String storeCode = "AA";
    private String location = "TESTLOCATION";
    private Branch branch = new TestBranchBuilder().build();

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

    public Store build(){
        return new StoreBuilder().createStore(
                name,
                storeCode,
                location,
                branch
        );
    }
}
