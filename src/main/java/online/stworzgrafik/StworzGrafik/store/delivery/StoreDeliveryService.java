package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreDeliveryService {
    ResponseStoreDeliveryDTO findByStoreId(@NotNull Long storeId);
    ResponseStoreDeliveryDTO findById(@NotNull Long storeDeliveryId);
    ResponseStoreDeliveryDTO update(@NotNull Long storeId, UpdateStoreDeliveryDTO dto);
}
