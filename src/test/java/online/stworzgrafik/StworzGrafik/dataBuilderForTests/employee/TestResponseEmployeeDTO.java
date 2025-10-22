package online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee;

import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;

import java.time.LocalDateTime;

public class TestResponseEmployeeDTO {
    private Long id = 1L;
    private String firstName = "TESTRESPONSEFIRSTNAME";
    private String lastName = "TESTRESPONSELASTNAME";
    private Long sap = 1L;
    private Long storeId = 1L;
    private Long positionId = 1L;
    private boolean enable = true;
    private boolean canOperateCheckout = true;
    private boolean canOperateCredit = true;
    private boolean canOpenCloseStore = true;
    private boolean seller = true;
    private boolean manager = true;
    private LocalDateTime createdAt = LocalDateTime.of(2000,1,1,14,0);
    private LocalDateTime updatedAt = LocalDateTime.of(2019,10,5,14,0);

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

    public TestResponseEmployeeDTO withEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public TestResponseEmployeeDTO withCanOperateCheckout(boolean canOperateCheckout){
        this.canOperateCheckout = canOperateCheckout;
        return this;
    }

    public TestResponseEmployeeDTO withCanOperateCredit(boolean canOperateCredit){
        this.canOperateCredit = canOperateCredit;
        return this;
    }

    public TestResponseEmployeeDTO withCanOpenCloseStore(boolean canOpenCloseStore){
        this.canOpenCloseStore = canOpenCloseStore;
        return this;
    }

    public TestResponseEmployeeDTO withSeller(boolean seller){
        this.seller = seller;
        return this;
    }

    public TestResponseEmployeeDTO withManager(boolean manager){
        this.manager = manager;
        return this;
    }

    public TestResponseEmployeeDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseEmployeeDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public ResponseEmployeeDTO build(){
        return new ResponseEmployeeDTO(
                id,
                firstName,
                lastName,
                sap,
                storeId,
                positionId,
                enable,
                canOperateCheckout,
                canOperateCredit,
                canOpenCloseStore,
                seller,
                manager,
                createdAt,
                updatedAt
        );
    }
}
