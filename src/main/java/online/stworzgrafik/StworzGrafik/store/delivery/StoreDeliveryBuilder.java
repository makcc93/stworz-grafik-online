package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
final class StoreDeliveryBuilder {
    public StoreDelivery createStoreDelivery(
            Store store,
            @Nullable Employee primaryEmployeeId
    ){
        return StoreDelivery.builder()
                .store(store)
                .primaryEmployee(primaryEmployeeId)
                .build();
    }
}
