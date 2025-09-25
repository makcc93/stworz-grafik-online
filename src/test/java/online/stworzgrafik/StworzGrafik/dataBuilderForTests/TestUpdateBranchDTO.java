package online.stworzgrafik.StworzGrafik.dataBuilderForTests;

import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;

public class TestUpdateBranchDTO {
    private String name = "TESTUPDATEBRANCH";
    private boolean isEnable = true;

    public TestUpdateBranchDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestUpdateBranchDTO withIsEnable(boolean isEnable){
        this.isEnable = isEnable;
        return this;
    }

    public UpdateBranchDTO build(){
        return new UpdateBranchDTO(
                name,
                isEnable
        );
    }
}
