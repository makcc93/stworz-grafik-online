package online.stworzgrafik.StworzGrafik.dataBuilderForTests.position;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;

public class TestResponsePositionDTO {
    private Long id = 1L;
    private String name = "TESTRESPONSEPOSITION";
    private String description = "TESTREPONSEDESCRIPTION";

    public TestResponsePositionDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponsePositionDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestResponsePositionDTO withDescription(String description){
        this.description = description;
        return this;
    }

    public ResponsePositionDTO build(){
        return new ResponsePositionDTO(
                id,
                name,
                description
        );
    }
}
