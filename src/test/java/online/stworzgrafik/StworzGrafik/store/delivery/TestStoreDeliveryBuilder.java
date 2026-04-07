package online.stworzgrafik.StworzGrafik.store.delivery;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

import java.time.LocalDateTime;

public class TestStoreDeliveryBuilder {
    private Long id = null;
    private Store store = new TestStoreBuilder().build();
    private boolean hasDedicatedWarehouseman = true;
    private Employee primaryEmployee = new TestEmployeeBuilder().withStore(store).buildDefault();
    private StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = StoreWeeklyDeliverySchedule.createDefault();
    private LocalDateTime createdAt = LocalDateTime.now();
    private Long createdByUserId = null;
    private LocalDateTime updatedAt = null;
    private Long updatedByUserId = null;

    public TestStoreDeliveryBuilder withId(Long id){
        this.id = id;
        return this;
    }

    public TestStoreDeliveryBuilder withStore(Store store) {
        this.store = store;
        return this;
    }

    public TestStoreDeliveryBuilder withHasDedicatedWarehouseman(boolean hasDedicatedWarehouseman) {
        this.hasDedicatedWarehouseman = hasDedicatedWarehouseman;
        return this;
    }

    public TestStoreDeliveryBuilder withPrimaryEmployee(Employee primaryEmployee) {
        this.primaryEmployee = primaryEmployee;
        return this;
    }

    public TestStoreDeliveryBuilder withStoreWeeklyDeliverySchedule(StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule) {
        this.storeWeeklyDeliverySchedule = storeWeeklyDeliverySchedule;
        return this;
    }

    public TestStoreDeliveryBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TestStoreDeliveryBuilder withCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
        return this;
    }

    public TestStoreDeliveryBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public TestStoreDeliveryBuilder withUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
        return this;
    }

    public StoreDelivery build(){
        return new StoreDelivery(
                id,
                store,
                hasDedicatedWarehouseman,
                primaryEmployee,
                storeWeeklyDeliverySchedule,
                createdAt,
                createdByUserId,
                updatedAt,
                updatedByUserId
        );
    }
}
