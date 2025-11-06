package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;

public class TestUpdateRegionDTO {
    private String name = "UPDATEREGION";
    private boolean enable = true;

    public TestUpdateRegionDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestUpdateRegionDTO withIsEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public UpdateRegionDTO build(){
        return new UpdateRegionDTO(
                name,
                enable
        );
    }
}
