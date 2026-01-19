package online.stworzgrafik.StworzGrafik.store;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.DTO.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface StoreService {
    List<ResponseStoreDTO> findAll();
    ResponseStoreDTO findById(@NotNull Long storeId);
    List<ResponseStoreDTO> findByCriteria(@Nullable StoreSpecificationDTO dto);
    ResponseStoreDTO createStore(@NotNull @Valid CreateStoreDTO createStoreDTO);
    ResponseStoreDTO update(@NotNull Long storeId,@NotNull @Valid UpdateStoreDTO updateStoreDTO);
    boolean existsById(@NotNull Long storeId);
    boolean existsByNameAndCode(@NotNull @Valid StoreNameAndCodeDTO storeNameAndCodeDTO);
    void delete(@NotNull Long storeId);
    ResponseStoreDTO save(@NotNull @Valid Store store);
}
