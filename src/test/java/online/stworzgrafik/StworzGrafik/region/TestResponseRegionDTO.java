package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;

public class TestResponseRegionDTO {
    private Long id = 1L;
    private String name = "TESTRESPONSEREGION";
    private boolean enable = true;

    public TestResponseRegionDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseRegionDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestResponseRegionDTO withIsEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public ResponseRegionDTO build(){
        return new ResponseRegionDTO(
                id,
                name,
                enable
        );
    }
}
