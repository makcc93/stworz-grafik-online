package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseDetailStoreDTO;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class StoreServiceImpl implements StoreService{
    private final StoreRepository storeRepository;
    private final StoreBuilder storeBuilder;
    private final StoreMapper storeMapper;

    public StoreServiceImpl(StoreRepository storeRepository, StoreBuilder storeBuilder, StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.storeBuilder = storeBuilder;
        this.storeMapper = storeMapper;
    }

    @Override
    public ResponseDetailStoreDTO create(CreateStoreDTO createStoreDTO) {
        ArgumentNullChecker.check(createStoreDTO);

        if (exists(createStoreDTO.name(),createStoreDTO.storeCode())){
            throw new IllegalArgumentException("Store with this name and store code already exist");
        }

        Store store = storeBuilder.createStore(
            createStoreDTO.name(),
            createStoreDTO.storeCode(),
            createStoreDTO.location(),
            createStoreDTO.branch(),
            createStoreDTO.region(),
            createStoreDTO.openForClientsHour(),
            createStoreDTO.closeForClientsHour()
        );

        Store savedStore = storeRepository.save(store);

        return storeMapper.toDetailStoreDto(savedStore);
    }

    @Override
    public boolean exists(Long id){
        ArgumentNullChecker.check(id,"Id");

        return storeRepository.existsById(id);
    }

    @Override
    public boolean exists(String storeName,String storeCode){
        ArgumentNullChecker.checkAll(storeName,storeCode);

        return storeRepository.existsByNameAndStoreCode(storeName,storeCode);
    }
}
