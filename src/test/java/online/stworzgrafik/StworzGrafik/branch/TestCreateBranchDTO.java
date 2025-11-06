package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;

public class TestCreateBranchDTO {
    private String name = "TESTCREATEBRANCH";
    private Long regionId = 1L;

    public TestCreateBranchDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestCreateBranchDTO withRegionId(Long regionId){
        this.regionId = regionId;
        return this;
    }

    public CreateBranchDTO build(){
        return new CreateBranchDTO(
                name,
                regionId
        );
    }
}
