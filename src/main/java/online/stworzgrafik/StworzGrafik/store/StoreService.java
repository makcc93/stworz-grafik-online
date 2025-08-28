package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseDetailStoreDTO;

public interface StoreService {
    ResponseDetailStoreDTO create(CreateStoreDTO createStoreDTO);
}
