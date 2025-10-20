package online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee;

import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.store.Store;

public class TestEmployeeBuilder {
    private String firstName = "TESTFIRSTNAME";
    private String lastName = "TESTLASTNAME";
    private Long sap = 1L;
    private Store store = new TestStoreBuilder().build();
    private Position position = new TestPositionBuilder().build();

    public TestEmployeeBuilder withFirstName(String firstName){
        this.firstName = firstName;
        return this;
    }

    public TestEmployeeBuilder withLastName(String lastName){
        this.lastName = lastName;
        return this;
    }

    public TestEmployeeBuilder withSap(Long sap){
        this.sap = sap;
        return this;
    }

    public TestEmployeeBuilder withStore(Store store){
        this.store = store;
        return this;
    }

    public TestEmployeeBuilder withPosition(Position position){
        this.position = position;
        return this;
    }

    public Employee build(){
        return new EmployeeBuilder().createEmployee(
                firstName,
                lastName,
                sap,
                store,
                position
        );
    }
}
