package online.stworzgrafik.StworzGrafik.store.storeDetails;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.CreateStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.ResponseStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.UpdateStoreDetailsDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class StoreDetailsServiceImpl implements StoreDetailsService{

    private final StoreDetailsRepository storeDetailsRepository;
    private final StoreEntityService storeService;
    private final StoreDetailsMapper mapper;

    public ResponseStoreDetailsDTO findByStoreId(Long storeId) {
        return storeDetailsRepository.findByStoreId(storeId)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Store details not found for store id: " + storeId
                ));
    }

    public ResponseStoreDetailsDTO create(CreateStoreDetailsDTO dto) {
        Store store = storeService.getEntityById(dto.storeId());

        if (storeDetailsRepository.existsByStoreId(dto.storeId())) {
            throw new IllegalStateException(
                    "Store details already exist for store id: " + dto.storeId()
            );
        }

        StoreDetails details = mapper.toEntity(dto, store);
        StoreDetails saved = storeDetailsRepository.save(details);

        return mapper.toDto(saved);
    }

    public ResponseStoreDetailsDTO update(Long storeId, UpdateStoreDetailsDTO dto) {
        StoreDetails details = storeDetailsRepository.findByStoreId(storeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Store details not found for store id: " + storeId
                ));

        mapper.updateStoreDetailsFromDTO(dto, details);
        StoreDetails updated = storeDetailsRepository.save(details);

        return mapper.toDto(updated);
    }

    public void delete(Long storeId) {
        StoreDetails details = storeDetailsRepository.findByStoreId(storeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Store details not found for store id: " + storeId
                ));

        storeDetailsRepository.delete(details);
    }
}
