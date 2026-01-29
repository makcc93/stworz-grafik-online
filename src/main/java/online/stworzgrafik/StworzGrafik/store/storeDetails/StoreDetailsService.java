package online.stworzgrafik.StworzGrafik.store.storeDetails;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.CreateStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.ResponseStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.UpdateStoreDetailsDTO;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreDetailsService {
    ResponseStoreDetailsDTO findByStoreId(@NotNull Long storeId);
    ResponseStoreDetailsDTO create(CreateStoreDetailsDTO dto);
    ResponseStoreDetailsDTO update(Long storeId, UpdateStoreDetailsDTO dto);
    void delete(Long storeId);
}
