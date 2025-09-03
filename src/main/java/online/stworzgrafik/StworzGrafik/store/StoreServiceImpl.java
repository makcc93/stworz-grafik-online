package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    public List<ResponseStoreDTO> findAll() {
        List<Store> stores = storeRepository.findAll();

        return stores.stream()
                .map(storeMapper::toResponseStoreDto)
                .sorted(Comparator.comparing(ResponseStoreDTO::storeCode))
                .toList();
    }

    @Override
    public ResponseStoreDTO findById(Long id) {
        ArgumentNullChecker.check(id,"Id");

        Store store = storeRepository.findById(id).orElseThrow();

        return storeMapper.toResponseStoreDto(store);
    }

    @Override
    public ResponseStoreDTO create(CreateStoreDTO createStoreDTO) {
        ArgumentNullChecker.check(createStoreDTO);

        StoreNameAndCodeDTO storeNameAndCode = storeMapper.toStoreNameAndCodeDTO(createStoreDTO);

        if (exists(storeNameAndCode)){
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

        return storeMapper.toResponseStoreDto(savedStore);
    }

    @Override
    public ResponseStoreDTO update(Long id, UpdateStoreDTO updateStoreDTO) {
        ArgumentNullChecker.check(id,"Id");
        ArgumentNullChecker.check(updateStoreDTO);

        Store store = storeRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        storeMapper.updateStoreFromDTO(updateStoreDTO,store);

        Store saved = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(saved);
    }

    @Override
    public boolean exists(Long id){
        ArgumentNullChecker.check(id,"Id");

        return storeRepository.existsById(id);
    }

    @Override
    public boolean exists(StoreNameAndCodeDTO storeNameAndCodeDTO){
        ArgumentNullChecker.checkAll(storeNameAndCodeDTO);

        return storeRepository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode());
    }

    @Override
    public void delete(Long storeId) {
        ArgumentNullChecker.check(storeId,"Store id");

        storeRepository.deleteById(storeId);
    }

    @Override
    public Store saveEntity(Store store) {
        ArgumentNullChecker.check(store,"Store");

        return storeRepository.save(store);
    }

    @Override
    public ResponseStoreDTO saveDto(StoreNameAndCodeDTO storeNameAndCodeDTO) {
        ArgumentNullChecker.check(storeNameAndCodeDTO);

        Store entity = storeMapper.toEntity(storeNameAndCodeDTO);

        Store savedEntity = storeRepository.save(entity);

        return storeMapper.toResponseStoreDto(savedEntity);
    }
}
