package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class StoreService{
    private final StoreRepository storeRepository;
    private final StoreBuilder storeBuilder;
    private final StoreMapper storeMapper;
    private final BranchRepository branchRepository;
    private final NameValidatorService nameValidatorService;

    public StoreService(StoreRepository storeRepository, StoreBuilder storeBuilder, StoreMapper storeMapper, BranchRepository branchRepository, NameValidatorService nameValidatorService) {
        this.storeRepository = storeRepository;
        this.storeBuilder = storeBuilder;
        this.storeMapper = storeMapper;
        this.branchRepository = branchRepository;
        this.nameValidatorService = nameValidatorService;
    }

    public List<ResponseStoreDTO> findAll() {
        List<Store> stores = storeRepository.findAll();

        return stores.stream()
                .map(storeMapper::toResponseStoreDto)
                .sorted(Comparator.comparing(ResponseStoreDTO::storeCode))
                .toList();
    }

    public ResponseStoreDTO findById(Long id) {
        Objects.requireNonNull(id, "Id cannot be null");

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + id));

        return storeMapper.toResponseStoreDto(store);
    }

    public ResponseStoreDTO createStore(CreateStoreDTO createStoreDTO) {
        Objects.requireNonNull(createStoreDTO);

        ifStoreAlreadyExist(createStoreDTO);

        Branch branch = branchRepository.findById(createStoreDTO.branchId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find branch by id " + createStoreDTO.branchId()));

        String validatedName = nameValidatorService.validate(createStoreDTO.name(), ObjectType.STORE);

        Store store = storeBuilder.createStore(
                validatedName,
                createStoreDTO.storeCode(),
                createStoreDTO.location(),
                branch
        );

        Store savedStore = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(savedStore);
    }

    public ResponseStoreDTO update(Long id, UpdateStoreDTO updateStoreDTO) {
        Objects.requireNonNull(id,"Id cannot be null");
        Objects.requireNonNull(updateStoreDTO);

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store to update by id " + id));

        if (updateStoreDTO.name() != null){
            String validatedName = nameValidatorService.validate(updateStoreDTO.name(), ObjectType.STORE);
            store.setName(validatedName);
        }

        storeMapper.updateStoreFromDTO(updateStoreDTO,store);

        whenBranchIsUpdated(updateStoreDTO, store);

        Store saved = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(saved);
    }

    public boolean exists(Long id){
        Objects.requireNonNull(id,"Id cannot be null");

        return storeRepository.existsById(id);
    }

    public boolean exists(StoreNameAndCodeDTO storeNameAndCodeDTO){
        Objects.requireNonNull(storeNameAndCodeDTO);

        return storeRepository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode());
    }

    public void delete(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        if (!exists(id)){
            throw new EntityNotFoundException("Store with id " + id +" does not exist");
        }

        storeRepository.deleteById(id);
    }

    public Store saveEntity(Store store) {
        Objects.requireNonNull(store,"Store cannot be null");

        return storeRepository.save(store);
    }

    public ResponseStoreDTO saveDto(StoreNameAndCodeDTO storeNameAndCodeDTO) {
       Objects.requireNonNull(storeNameAndCodeDTO);

        Store entity = storeMapper.toEntity(storeNameAndCodeDTO);

        Store savedEntity = storeRepository.save(entity);

        return storeMapper.toResponseStoreDto(savedEntity);
    }

    private void ifStoreAlreadyExist(CreateStoreDTO createStoreDTO) {
        String name = createStoreDTO.name();
        if (storeRepository.existsByName(name)){
            throw new EntityExistsException("Store with name " + name + " already exists");
        }

        String storeCode = createStoreDTO.storeCode();
        if (storeRepository.existsByStoreCode(storeCode)){
            throw new EntityExistsException("Store with code " + storeCode + " already exists");
        }
    }

    private void whenBranchIsUpdated(UpdateStoreDTO updateStoreDTO, Store store) {
        if (updateStoreDTO.branchId() != null){
            Branch branch = branchRepository.findById(updateStoreDTO.branchId())
                    .orElseThrow(() -> new EntityNotFoundException("Branch not found by id " + updateStoreDTO.branchId()));

            store.setBranch(branch);
        }
    }
}
