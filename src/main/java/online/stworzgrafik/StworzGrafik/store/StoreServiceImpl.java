package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseDetailStoreDTO;
import org.springframework.stereotype.Service;

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

        Store store = storeBuilder.createStore(
            createStoreDTO.name(),
            createStoreDTO.storeCode(),
            createStoreDTO.location(),
            createStoreDTO.branch(),
            createStoreDTO.region(),
            createStoreDTO.openHour(),
            createStoreDTO.closeHour()
        );

        Store savedStore = storeRepository.save(store);

        return storeMapper.toDetailStoreDto(savedStore);
    }
}
