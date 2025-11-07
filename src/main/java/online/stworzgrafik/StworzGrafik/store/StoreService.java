package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;

@Service
@Validated
@AllArgsConstructor
public class StoreService{
    private final StoreRepository storeRepository;
    private final StoreBuilder storeBuilder;
    private final StoreMapper storeMapper;
    private final BranchService branchService;
    private final NameValidatorService nameValidatorService;
    private final EntityManager entityManager;

    public List<ResponseStoreDTO> findAll() {
        List<Store> stores = storeRepository.findAll();

        return stores.stream()
                .map(storeMapper::toResponseStoreDto)
                .sorted(Comparator.comparing(ResponseStoreDTO::storeCode))
                .toList();
    }

    public ResponseStoreDTO findById(@Valid Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + id));

        return storeMapper.toResponseStoreDto(store);
    }

    public ResponseStoreDTO createStore(@Valid CreateStoreDTO createStoreDTO) {
        ifStoreAlreadyExist(createStoreDTO);

        Branch branch = getBranchReference(createStoreDTO);

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

    public ResponseStoreDTO update(@Valid Long id, @Valid UpdateStoreDTO updateStoreDTO) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store to update by id " + id));

        if (updateStoreDTO.name() != null){
            String validatedName = nameValidatorService.validate(updateStoreDTO.name(), ObjectType.STORE);
            store.setName(validatedName);
        }

        storeMapper.updateStoreFromDTO(updateStoreDTO,store);

        updateBranch(updateStoreDTO, store);

        Store saved = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(saved);
    }

    public boolean exists(@Valid Long id){
        return storeRepository.existsById(id);
    }

    public boolean exists(@Valid StoreNameAndCodeDTO storeNameAndCodeDTO){
        return storeRepository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode());
    }

    public void delete(@Valid Long id) {
        if (!exists(id)){
            throw new EntityNotFoundException("Store with id " + id +" does not exist");
        }

        storeRepository.deleteById(id);
    }

    public Store save(@Valid Store store) {
        return storeRepository.save(store);
    }

    public ResponseStoreDTO saveDto(@Valid StoreNameAndCodeDTO storeNameAndCodeDTO) {
        Store entity = storeMapper.toEntity(storeNameAndCodeDTO);

        Store savedEntity = storeRepository.save(entity);

        return storeMapper.toResponseStoreDto(savedEntity);
    }

    private void ifStoreAlreadyExist(@Valid CreateStoreDTO createStoreDTO) {
        String name = createStoreDTO.name();
        if (storeRepository.existsByName(name)){
            throw new EntityExistsException("Store with name " + name + " already exists");
        }

        String storeCode = createStoreDTO.storeCode();
        if (storeRepository.existsByStoreCode(storeCode)){
            throw new EntityExistsException("Store with code " + storeCode + " already exists");
        }
    }

    private void updateBranch(@Valid UpdateStoreDTO updateStoreDTO, @Valid Store store) {
        if (updateStoreDTO.branchId() != null){
            Branch branch = getBranchReference(updateStoreDTO);

            store.setBranch(branch);
        }
    }

    private Branch getBranchReference(@Valid CreateStoreDTO createStoreDTO){
        if (!branchService.exists(createStoreDTO.branchId())){
            throw new EntityNotFoundException("Cannot find branch by id " + createStoreDTO.branchId());
        }

        return entityManager.getReference(Branch.class,createStoreDTO.branchId());
    }

    private Branch getBranchReference(@Valid UpdateStoreDTO updateStoreDTO){
        if (!branchService.exists(updateStoreDTO.branchId())){
            throw new EntityNotFoundException("Cannot find branch by id " + updateStoreDTO.branchId());
        }

        return entityManager.getReference(Branch.class,updateStoreDTO.branchId());
    }
}
