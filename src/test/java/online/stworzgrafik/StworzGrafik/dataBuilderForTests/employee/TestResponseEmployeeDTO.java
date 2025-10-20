package online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee;

import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;

public class TestResponseEmployeeDTO {
    private Long id = 1L;
    private String firstName = "TESTRESPONSEFIRSTNAME";
    private String lastName = "TESTRESPONSELASTNAME";
    private Long sap = 1L;
    private Long storeId = 1L;
    private Long positionId = 1L;

    public TestResponseEmployeeDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseEmployeeDTO withFirstName(String firstName){
        this.firstName = firstName;
        return this;
    }

    public TestResponseEmployeeDTO withLastName(String lastName){
        this.lastName = lastName;
        return this;
    }

    public TestResponseEmployeeDTO withSap(Long sap){
        this.sap = sap;
        return this;
    }

    public TestResponseEmployeeDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestResponseEmployeeDTO withPositionId(Long positionId){
        this.positionId = positionId;
        return this;
    }

    public ResponseEmployeeDTO build(){
        return new ResponseEmployeeDTO(
                id,
                firstName,
                lastName,
                sap,
                storeId,
                positionId
        );
    }
}
