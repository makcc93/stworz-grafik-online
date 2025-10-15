package online.stworzgrafik.StworzGrafik.dataBuilderForTests.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;

import java.time.LocalTime;

public class TestUpdateStoreDTO {
    private String name = "TESTUPDATESTORE";
    private String storeCode = "BB";
    private String location = "TESTUPDATELOCATION";
    private Branch branch = new TestBranchBuilder().build();
    private boolean enable = true;
    private Long storeManagerId = 2L;

    public TestUpdateStoreDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestUpdateStoreDTO withStoreCode(String storeCode){
        this.storeCode = storeCode;
        return this;
    }

    public TestUpdateStoreDTO withLocation(String location){
        this.location = location;
        return this;
    }

    public TestUpdateStoreDTO withBranch(Branch branch){
        this.branch = branch;
        return this;
    }

    public TestUpdateStoreDTO withIsEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public TestUpdateStoreDTO withStoreManagerId(Long storeManagerId){
        this.storeManagerId = storeManagerId;
        return this;
    }

    public UpdateStoreDTO build(){
        return new UpdateStoreDTO(
                name,
                storeCode,
                location,
                branch.getId(),
                enable,
                storeManagerId
        );
    }
}
