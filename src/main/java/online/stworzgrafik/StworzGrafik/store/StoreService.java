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
    public List<ResponseStoreDTO> findAll();
    public ResponseStoreDTO findById(@NotNull Long id);
    public ResponseStoreDTO createStore(@NotNull @Valid CreateStoreDTO createStoreDTO);
    public ResponseStoreDTO update(@NotNull Long id, @NotNull @Valid UpdateStoreDTO updateStoreDTO);
    public boolean exists(@NotNull Long id);
    public boolean exists(@NotNull @Valid StoreNameAndCodeDTO storeNameAndCodeDTO);
    public void delete(@NotNull Long id);
    public ResponseStoreDTO save(@Valid Store store);
}
