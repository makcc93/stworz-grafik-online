package online.stworzgrafik.StworzGrafik.store.delivery;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.CreateStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreDeliveryServiceImpl implements StoreDeliveryService{
    private final UserAuthorizationService userAuthorizationService;
    private final StoreService storeService;
    private final StoreDeliveryRepository repository;

    //todo
    //dokoncz seriveImpl
    @Override
    public ResponseStoreDeliveryDTO create(Long storeId, CreateStoreDeliveryDTO dto) {
        verifyLoggedUserStoreAccess(storeId);

        if (storeService.)
    }

    @Override
    public ResponseStoreDeliveryDTO update(Long storeId, UpdateStoreDeliveryDTO dto) {
        verifyLoggedUserStoreAccess(storeId);
    }

    @Override
    public void delete(Long storeId, Long storeDeliveryId) {
        verifyLoggedUserStoreAccess(storeId);


    }

    @Override
    public ResponseStoreDeliveryDTO save(Long storeId, StoreDelivery storeDelivery) {
        verifyLoggedUserStoreAccess(storeId);
    }

    private void verifyLoggedUserStoreAccess(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }
}
