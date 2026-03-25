package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.Column;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.TestEmployeeProposalDaysOffBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.TestEmployeeProposalShiftsBuilder;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacation;
import online.stworzgrafik.StworzGrafik.employee.vacation.TestEmployeeVacationBuilder;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.TestScheduleDetailsBuilder;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestEmployeeBuilder {
    private Long id = 10L;
    private String firstName = "TESTFIRSTNAME";
    private String lastName = "TESTLASTNAME";
    private Long sap = 1L;
    private Store store = new TestStoreBuilder().build();
    private Position position = new TestPositionBuilder().build();
    private boolean enable;
    private boolean canOperateCheckout = false;
    private boolean canOperateCredit = false;
    private boolean canOpenCloseStore = false;
    private boolean canOperateDelivery = false;
    private boolean seller = false;
    private boolean manager = false;
    private boolean cashier = false;
    private boolean warehouseman = false;
    private boolean pok = false;
    private LocalDateTime createdAt = LocalDateTime.of(2020,10,20,20,10);
    private LocalDateTime updatedAt = null;
    private List<EmployeeVacation> employeeVacations = Collections.emptyList();
    private List<EmployeeProposalDaysOff> employeeProposalDaysOff = Collections.emptyList();
    private List<EmployeeProposalShifts> employeeProposalShifts = Collections.emptyList();
    private List<ScheduleDetails> scheduleDetails = Collections.emptyList();

    public TestEmployeeBuilder withId(Long id){
        this.id = id;
        return this;
    }

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
    public TestEmployeeBuilder withCanOperateCheckout(boolean canOperateCheckout) {
        this.canOperateCheckout = canOperateCheckout;
        return this;
    }

    public TestEmployeeBuilder withCanOperateCredit(boolean canOperateCredit) {
        this.canOperateCredit = canOperateCredit;
        return this;
    }

    public TestEmployeeBuilder withCanOpenCloseStore(boolean canOpenCloseStore) {
        this.canOpenCloseStore = canOpenCloseStore;
        return this;
    }

    public TestEmployeeBuilder withCanOperateDelivery(boolean canOperateDelivery) {
        this.canOperateDelivery = canOperateDelivery;
        return this;
    }

    public TestEmployeeBuilder withSeller(boolean seller) {
        this.seller = seller;
        return this;
    }

    public TestEmployeeBuilder withManager(boolean manager) {
        this.manager = manager;
        return this;
    }

    public TestEmployeeBuilder withCashier(boolean cashier) {
        this.cashier = cashier;
        return this;
    }

    public TestEmployeeBuilder withWarehouseman(boolean warehouseman) {
        this.warehouseman = warehouseman;
        return this;
    }

    public TestEmployeeBuilder withPok(boolean pok) {
        this.pok = pok;
        return this;
    }

    public TestEmployeeBuilder withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestEmployeeBuilder withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public TestEmployeeBuilder withEmployeeVacation(List<EmployeeVacation> employeeVacations){
        this.employeeVacations = employeeVacations;
        return this;
    }

    public TestEmployeeBuilder withEmployeeProposalDaysOff(List<EmployeeProposalDaysOff> employeeProposalDaysOff){
        this.employeeProposalDaysOff = employeeProposalDaysOff;
        return this;
    }

    public TestEmployeeBuilder withEmployeeProposalShifts(List<EmployeeProposalShifts> employeeProposalShifts){
        this.employeeProposalShifts = employeeProposalShifts;
        return this;
    }

    public TestEmployeeBuilder withScheduleDetails(List<ScheduleDetails> scheduleDetails){
        this.scheduleDetails = scheduleDetails;
        return this;
    }

    public Employee buildDefault(){
        return new EmployeeBuilder().createEmployee(
                firstName,
                lastName,
                sap,
                store,
                position
        );
    }

    public Employee build(){
        return new Employee(
                id,
                firstName,
                lastName,
                sap,
                store,
                position,
                enable,
                canOperateCheckout,
                canOperateCredit,
                canOpenCloseStore,
                canOperateDelivery,
                seller,
                manager,
                cashier,
                warehouseman,
                pok,
                createdAt,
                updatedAt,
                employeeVacations,
                employeeProposalDaysOff,
                employeeProposalShifts,
                scheduleDetails
        );
    }
}
