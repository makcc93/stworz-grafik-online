package online.stworzgrafik.StworzGrafik.store.delivery;

import online.stworzgrafik.StworzGrafik.store.delivery.DTO.CreateStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.springframework.stereotype.Service;

@Service
public class StoreDeliveryServiceImpl implements StoreDeliveryService{
    @Override
    public ResponseStoreDeliveryDTO create(Long storeId, CreateStoreDeliveryDTO dto) {
        return null;
    }

    @Override
    public ResponseStoreDeliveryDTO update(Long storeId, UpdateStoreDeliveryDTO dto) {
        return null;
    }

    @Override
    public void delete(Long storeId, Long storeDeliveryId) {

    }

    @Override
    public ResponseStoreDeliveryDTO save(Long storeId, StoreDelivery storeDelivery) {
        return null;
    }
}
