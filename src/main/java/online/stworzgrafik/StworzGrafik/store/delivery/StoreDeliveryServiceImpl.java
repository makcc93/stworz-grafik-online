package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreDeliveryServiceImpl implements StoreDeliveryService{
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeEntityService;
    private final StoreDeliveryRepository repository;
    private final StoreDeliveryMapper mapper;

    @Override
    public ResponseStoreDeliveryDTO findByStoreId(Long storeId) {
        verifyLoggedUserStoreAccess(storeId);

        Store store = storeEntityService.getEntityById(storeId);
        StoreDelivery storeDelivery = store.getDelivery();

        return mapper.toDTO(storeDelivery);
    }

    @Override
    public ResponseStoreDeliveryDTO findById(Long storeDeliveryId) {
        StoreDelivery storeDelivery = repository.findById(storeDeliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Store Delivery by its id " + storeDeliveryId));

        return mapper.toDTO(storeDelivery);
    }

    @Override
    public ResponseStoreDeliveryDTO update(Long storeId,UpdateStoreDeliveryDTO dto) {
        verifyLoggedUserStoreAccess(storeId);

        Store store = storeEntityService.getEntityById(storeId);

        StoreDelivery storeDelivery = store.getDelivery();

        mapper.update(dto,storeDelivery);

        StoreDelivery saved = repository.save(storeDelivery);

        return mapper.toDTO(saved);
    }

    @Override
    public boolean hasDedicatedWarehouseman(Long storeId) {
        verifyLoggedUserStoreAccess(storeId);
        Store store = storeEntityService.getEntityById(storeId);

        StoreDelivery storeDelivery = store.getDelivery();

        return storeDelivery.getHasDedicatedWarehouseman();
    }

    private void verifyLoggedUserStoreAccess(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }
}
