package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;

public class TestResponseBranchDTO {
    private Long id = 1L;
    private String name = "TESTBRANCH";
    private boolean enable = true;
    private Long regionId = 1L;
    private String regionName = "TESTREGIONNAME";

    public TestResponseBranchDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseBranchDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestResponseBranchDTO withIsEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public TestResponseBranchDTO withRegionId(Long regionId){
        this.regionId = regionId;
        return this;
    }

    public TestResponseBranchDTO withRegionName(String regionName){
        this.regionName = regionName;
        return this;
    }

    public ResponseBranchDTO build(){
        return new ResponseBranchDTO(
                id,
                name,
                enable,
                regionId,
                regionName
        );
    }
}
