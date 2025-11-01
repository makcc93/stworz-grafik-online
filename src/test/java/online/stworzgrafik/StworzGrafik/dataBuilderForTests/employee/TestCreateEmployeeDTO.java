package online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee;

import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;

public class TestCreateEmployeeDTO {
    private String firstName = "TESTCREATEFIRSTNAME";
    private String lastName = "TESTCREATELASTNAME";
    private Long sap = 11112222L;
    private Long storeId = 1L;
    private Long positionId = 1L;

    public TestCreateEmployeeDTO withFirstName(String firstName){
        this.firstName = firstName;
        return this;
    }

    public TestCreateEmployeeDTO withLastName(String lastName){
        this.lastName = lastName;
        return this;
    }

    public TestCreateEmployeeDTO withSap(Long sap){
        this.sap = sap;
        return this;
    }

    public TestCreateEmployeeDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestCreateEmployeeDTO withPositionId(Long positionId){
        this.positionId = positionId;
        return this;
    }

    public CreateEmployeeDTO build(){
        return new CreateEmployeeDTO(
          firstName,
          lastName,
          sap,
          storeId,
          positionId
        );
    }
}
