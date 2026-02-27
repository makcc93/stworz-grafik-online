package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.CreateStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreDeliveryService {
    ResponseStoreDeliveryDTO create(@NotNull Long storeId, @Valid @NotNull CreateStoreDeliveryDTO dto);
    ResponseStoreDeliveryDTO update(@NotNull Long storeId, UpdateStoreDeliveryDTO dto);
    void delete(@NotNull Long storeId, @NotNull Long storeDeliveryId);
    ResponseStoreDeliveryDTO save(@NotNull Long storeId, StoreDelivery storeDelivery);
}
