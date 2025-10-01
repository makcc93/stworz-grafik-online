package online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;

public class TestBranchBuilder {
    private String name = "TESTBRANCH";
    private Region region = new TestRegionBuilder().build();

    public TestBranchBuilder withName(String name){
        this.name = name;
        return this;
    }

    public TestBranchBuilder withRegion(Region region){
        this.region = region;
        return this;
    }

    public Branch build(){
        return new BranchBuilder().createBranch(
                name,
                region
        );
    }
}
