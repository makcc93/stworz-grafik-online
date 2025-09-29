package online.stworzgrafik.StworzGrafik.dataBuilderForTests.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.RegionType;

import java.time.LocalTime;

public class TestCreateStoreDTO {
    private String name = "TESTCREATESTORE";
    private String storeCode = "CC";
    private String location = "TESTCREATELOCATION";
    private Branch branch = new TestBranchBuilder().build();
    private LocalTime openHour = LocalTime.of(8,0);
    private LocalTime closeHour = LocalTime.of(21,0);

    public TestCreateStoreDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestCreateStoreDTO withStoreCode(String storeCode){
        this.storeCode = storeCode;
        return this;
    }

    public TestCreateStoreDTO withLocation(String location){
        this.location = location;
        return this;
    }

    public TestCreateStoreDTO withBranch(Branch branch){
        this.branch = branch;
        return this;
    }


    public TestCreateStoreDTO withOpenHour(LocalTime openHour){
        this.openHour = openHour;
        return this;
    }

    public TestCreateStoreDTO withCloseHour(LocalTime closeHour){
        this.closeHour = closeHour;
        return this;
    }

    public CreateStoreDTO build(){
        return new CreateStoreDTO(
                name,
                storeCode,
                location,
                branch.getId(),
                openHour,
                closeHour
        );
    }
}
