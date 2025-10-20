package online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee;

import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;

public class TestUpdateEmployeeDTO {
    private String firstName = "TESTUPDATEFIRSTNAME";
    private String lastName = "TESTUPDATELASTNAME";
    private Long sap = 1L;
    private Long storeId = 1L;
    private Long positionId = 1L;
    private boolean enable = true;
    private boolean canOperateCheckout = true;
    private boolean canOperateCredit = true;
    private boolean canOpenCloseStore = true;
    private boolean seller = true;
    private boolean manager = true;

    public TestUpdateEmployeeDTO withFirstName(String firstName){
        this.firstName = firstName;
        return this;
    }

    public TestUpdateEmployeeDTO withLastName(String lastName){
        this.lastName = lastName;
        return this;
    }

    public TestUpdateEmployeeDTO withSap(Long sap){
        this.sap = sap;
        return this;
    }

    public TestUpdateEmployeeDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestUpdateEmployeeDTO withPositionId(Long positionId){
        this.positionId = positionId;
        return this;
    }

    public TestUpdateEmployeeDTO withEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public TestUpdateEmployeeDTO withCanOperateCheckout(boolean canOperateCheckout){
        this.canOperateCheckout = canOperateCheckout;
        return this;
    }

    public TestUpdateEmployeeDTO withCanOperateCredit(boolean canOperateCredit){
        this.canOperateCredit = canOperateCredit;
        return this;
    }

    public TestUpdateEmployeeDTO withCanOpenCloseStore(boolean canOpenCloseStore){
        this.canOpenCloseStore = canOpenCloseStore;
        return this;
    }

    public TestUpdateEmployeeDTO withSeller(boolean seller){
        this.seller = seller;
        return this;
    }

    public TestUpdateEmployeeDTO withManager(boolean manager){
        this.manager = manager;
        return this;
    }

    public UpdateEmployeeDTO build(){
        return new UpdateEmployeeDTO(
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
                manager
        );
    }
}
