package online.stworzgrafik.StworzGrafik.dataBuilderForTests.position;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;

public class TestUpdatePositionDTO {
    private String name = "UPDATE POSITION NAME";
    private String description = "UPDATE POSITION NAME DESCRIPTION";

    public TestUpdatePositionDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestUpdatePositionDTO withDescription(String description){
        this.description = description;
        return this;
    }

    public UpdatePositionDTO build(){
        return new UpdatePositionDTO(
                name,
                description
        );
    }
}
