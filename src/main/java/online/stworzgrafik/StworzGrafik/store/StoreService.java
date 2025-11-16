package online.stworzgrafik.StworzGrafik.store;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface StoreService {
    List<ResponseStoreDTO> findAll();
    ResponseStoreDTO findById(@NotNull Long id);
    ResponseStoreDTO createStore(@NotNull @Valid CreateStoreDTO createStoreDTO);
    ResponseStoreDTO update(@NotNull Long id,@NotNull @Valid UpdateStoreDTO updateStoreDTO);
    boolean existsById(@NotNull Long id);
    boolean existsByNameAndCode(@NotNull @Valid StoreNameAndCodeDTO storeNameAndCodeDTO);
    void delete(@NotNull Long id);
    ResponseStoreDTO save(@NotNull @Valid Store store);
}
