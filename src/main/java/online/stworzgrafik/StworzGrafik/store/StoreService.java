package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;

import java.util.List;

public interface StoreService {
    List<ResponseStoreDTO> findAll();
    ResponseStoreDTO findById(Long id);
    ResponseStoreDTO create(CreateStoreDTO createStoreDTO);
    ResponseStoreDTO update(Long id, UpdateStoreDTO updateStoreDTO);
    boolean exists(Long id);
    boolean exists(StoreNameAndCodeDTO storeNameAndCodeDTO);
    void delete(Long storeId);
    Store saveEntity(Store store);
    ResponseStoreDTO saveDto(StoreNameAndCodeDTO storeNameAndCodeDTO);
}
