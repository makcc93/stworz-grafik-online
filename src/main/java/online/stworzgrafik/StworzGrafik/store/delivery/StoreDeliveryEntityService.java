package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreDeliveryEntityService {
    StoreDelivery save(@NotNull StoreDelivery storeDelivery);
}
