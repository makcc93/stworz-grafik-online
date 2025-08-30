package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseDetailStoreDTO;
import org.springframework.cglib.core.Local;

import java.time.LocalTime;

public interface StoreService {
    ResponseDetailStoreDTO create(CreateStoreDTO createStoreDTO);
    boolean exists(Long id);
    boolean exists(String storeName,String storeCode);
}
