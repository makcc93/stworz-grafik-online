package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;

public class TestUpdateBranchDTO {
    private String name = "TESTUPDATEBRANCH";
    private boolean enable = true;

    public TestUpdateBranchDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestUpdateBranchDTO withIsEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public UpdateBranchDTO build(){
        return new UpdateBranchDTO(
                name,
                enable
        );
    }
}
