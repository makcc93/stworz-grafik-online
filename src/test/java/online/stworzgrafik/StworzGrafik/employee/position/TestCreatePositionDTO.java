package online.stworzgrafik.StworzGrafik.employee.position;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;

public class TestCreatePositionDTO {
    private String name = "TESTCREATENAME";
    private String description = "TESTCREATEDESCRIPTION";

    public TestCreatePositionDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestCreatePositionDTO withDescription(String description){
        this.description = description;
        return this;
    }

    public CreatePositionDTO build(){
        return new CreatePositionDTO(
                name,
                description
        );
    }
}
