package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.RegionSpecificationDTO;

public class TestRegionSpecificationDTO {
    private Long id = null;
    private String name = null;
    private Boolean enable = null;

    public TestRegionSpecificationDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestRegionSpecificationDTO withName(String name){
        this.name = name;
        return this;
    }
    public TestRegionSpecificationDTO withEnable(Boolean enable){
        this.enable = enable;
        return this;
    }

    public RegionSpecificationDTO build(){
        return new RegionSpecificationDTO(
                id,
                name,
                enable
        );
    }
}
