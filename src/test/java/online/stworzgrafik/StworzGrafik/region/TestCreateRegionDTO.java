package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;

public class TestCreateRegionDTO {
    private String name = "TESTCREATEREGION";

    public TestCreateRegionDTO withName(String name){
        this.name = name;
        return this;
    }

    public CreateRegionDTO build(){
        return new CreateRegionDTO(
                name
        );
    }
}
