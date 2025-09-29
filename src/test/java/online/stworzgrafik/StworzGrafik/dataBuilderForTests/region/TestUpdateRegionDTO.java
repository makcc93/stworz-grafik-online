package online.stworzgrafik.StworzGrafik.dataBuilderForTests.region;

import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;

public class TestUpdateRegionDTO {
    private String name = "UPDATEREGION";
    private boolean isEnable = true;

    public TestUpdateRegionDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestUpdateRegionDTO withIsEnable(boolean isEnable){
        this.isEnable = isEnable;
        return this;
    }

    public UpdateRegionDTO build(){
        return new UpdateRegionDTO(
                name,
                isEnable
        );
    }
}
