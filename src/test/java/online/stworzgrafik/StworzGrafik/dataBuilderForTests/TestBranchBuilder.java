package online.stworzgrafik.StworzGrafik.dataBuilderForTests;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;

public class TestBranchBuilder {
    private String name = "TESTBRANCH";

    public TestBranchBuilder withName(String name){
        this.name = name;
        return this;
    }

    public Branch build(){
        return new BranchBuilder().createBranch(
                name
        );
    }
}
