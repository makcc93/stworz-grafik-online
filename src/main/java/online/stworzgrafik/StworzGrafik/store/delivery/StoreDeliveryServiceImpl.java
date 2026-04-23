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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreDeliveryServiceImpl implements StoreDeliveryService, StoreDeliveryEntityService{
    private final UserAuthorizationService userAuthorizationService;
    private final StoreDeliveryRepository repository;
    private final StoreDeliveryMapper mapper;

    @Override
    public ResponseStoreDeliveryDTO findByStoreId(Long storeId) {
        verifyLoggedUserStoreAccess(storeId);

        StoreDelivery storeDelivery  = repository.findByStoreId(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store entity by id " + storeId));

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

        StoreDelivery storeDelivery  = repository.findByStoreId(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store delivery by store id " + storeId));

        mapper.update(dto,storeDelivery);

        StoreDelivery saved = repository.save(storeDelivery);

        return mapper.toDTO(saved);
    }

    @Override
    public boolean hasDedicatedWarehouseman(Long storeId) {
        verifyLoggedUserStoreAccess(storeId);

        StoreDelivery storeDelivery  = repository.findByStoreId(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store entity by id " + storeId));

        return storeDelivery.getHasDedicatedWarehouseman();
    }

    @Override
    @Transactional
    public void initializeDefault(Store store) {
        StoreDelivery delivery = StoreDelivery.builder()
                .store(store)
                .hasDedicatedWarehouseman(false)
                .storeWeeklyDeliverySchedule(StoreWeeklyDeliverySchedule.createDefault())
                .build();

        repository.save(delivery);
    }

    private void verifyLoggedUserStoreAccess(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }

    @Override
    public StoreDelivery save(StoreDelivery storeDelivery) {
        return repository.save(storeDelivery);
    }
}
