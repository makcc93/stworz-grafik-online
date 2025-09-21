package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StoreServiceImpl implements StoreService{
    private final StoreRepository storeRepository;
    private final StoreBuilder storeBuilder;
    private final StoreMapper storeMapper;
    private final BranchRepository branchRepository;

    public StoreServiceImpl(StoreRepository storeRepository, StoreBuilder storeBuilder, StoreMapper storeMapper, BranchRepository branchRepository) {
        this.storeRepository = storeRepository;
        this.storeBuilder = storeBuilder;
        this.storeMapper = storeMapper;
        this.branchRepository = branchRepository;
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

        isStoreAlreadyExist(createStoreDTO);

        Branch branch = branchRepository.findById(createStoreDTO.branchId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find branch by id " + createStoreDTO.branchId()));

        Store store = storeBuilder.createStore(
            createStoreDTO.name(),
            createStoreDTO.storeCode(),
            createStoreDTO.location(),
            branch,
            createStoreDTO.region(),
            createStoreDTO.openForClientsHour(),
            createStoreDTO.closeForClientsHour()
        );

        Store savedStore = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(savedStore);
    }

    private void isStoreAlreadyExist(CreateStoreDTO createStoreDTO) {
        StoreNameAndCodeDTO storeNameAndCode = storeMapper.toStoreNameAndCodeDTO(createStoreDTO);

        if (exists(storeNameAndCode)){
            throw new IllegalArgumentException("Store with this name and store code already exist");
        }
    }

    @Override
    public ResponseStoreDTO update(Long id, UpdateStoreDTO updateStoreDTO) {
        ArgumentNullChecker.check(id,"Id");
        ArgumentNullChecker.check(updateStoreDTO);

        Store store = storeRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        storeMapper.updateStoreFromDTO(updateStoreDTO,store);

        whenBranchIsUpdated(updateStoreDTO, store);

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


        if (!exists(storeId)){
            throw new EntityNotFoundException("Store with id " + storeId +" does not exist");
        }

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

    private void whenBranchIsUpdated(UpdateStoreDTO updateStoreDTO, Store store) {
        if (updateStoreDTO.branchId() != null){
            Branch branch = branchRepository.findById(updateStoreDTO.branchId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found by id " + updateStoreDTO.branchId()));

            store.setBranch(branch);
        }
    }
}
