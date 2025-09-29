package online.stworzgrafik.StworzGrafik.dataBuilderForTests.region;

import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestCreateStoreDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;

public class TestCreateRegionDTO {
    private String name = "TESTCREATEREGION";

    public CreateRegionDTO build(){
        return new CreateRegionDTO(
                name
        );
    }
}
