package online.stworzgrafik.StworzGrafik.dataBuilderForTests;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.RegionType;

import java.time.LocalTime;

public class TestUpdateStoreDTO {
    private String name = "TESTUPDATESTORE";
    private String storeCode = "BB";
    private String location = "TESTUPDATELOCATION";
    private Branch branch = new TestBranchBuilder().build();
    private RegionType region = RegionType.ZACHOD;
    private boolean isEnable = true;
    private Long storeManagerId = 2L;
    private LocalTime openHour = LocalTime.of(10,0);
    private LocalTime closeHour = LocalTime.of(18,0);

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

    public TestUpdateStoreDTO withRegion(RegionType region){
        this.region = region;
        return this;
    }

    public TestUpdateStoreDTO withIsEnable(boolean isEnable){
        this.isEnable = isEnable;
        return this;
    }

    public TestUpdateStoreDTO withStoreManagerId(Long storeManagerId){
        this.storeManagerId = storeManagerId;
        return this;
    }

    public TestUpdateStoreDTO withOpenHour(LocalTime openHour){
        this.openHour = openHour;
        return this;
    }

    public TestUpdateStoreDTO withCloseHour(LocalTime closeHour){
        this.closeHour = closeHour;
        return this;
    }

    public UpdateStoreDTO build(){
        return new UpdateStoreDTO(
                name,
                storeCode,
                location,
                branch.getId(),
                region,
                isEnable,
                storeManagerId,
                openHour,
                closeHour
        );
    }
}
