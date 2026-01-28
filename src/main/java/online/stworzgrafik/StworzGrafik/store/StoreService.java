package online.stworzgrafik.StworzGrafik.store;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface StoreService {
    Page<ResponseStoreDTO> findAll(Pageable pageable);
    ResponseStoreDTO findById(@NotNull Long storeId);
    Page<ResponseStoreDTO> findByCriteria(@Nullable StoreSpecificationDTO dto, Pageable pageable);
    ResponseStoreDTO createStore(@NotNull @Valid CreateStoreDTO createStoreDTO);
    ResponseStoreDTO update(@NotNull Long storeId,@NotNull @Valid UpdateStoreDTO updateStoreDTO);
    boolean existsById(@NotNull Long storeId);
    boolean existsByNameAndCode(@NotNull @Valid StoreNameAndCodeDTO storeNameAndCodeDTO);
    void delete(@NotNull Long storeId);
    ResponseStoreDTO save(@NotNull @Valid Store store);
}
