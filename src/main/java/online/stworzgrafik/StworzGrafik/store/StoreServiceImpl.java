package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
class StoreServiceImpl implements StoreService, StoreEntityService{
    private final StoreRepository storeRepository;
    private final StoreBuilder storeBuilder;
    private final StoreMapper storeMapper;
    private final BranchEntityService branchEntityService;
    private final NameValidatorService nameValidatorService;
    private final UserAuthorizationService userAuthorizationService;

    @Override
    public List<ResponseStoreDTO> findAll() {
        List<Store> stores = storeRepository.findAll();

        return stores.stream()
                .map(storeMapper::toResponseStoreDto)
                .sorted(Comparator.comparing(ResponseStoreDTO::storeCode))
                .toList();
    }

    @Override
    public ResponseStoreDTO findById(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + storeId));

        return storeMapper.toResponseStoreDto(store);
    }

    @Override
    public ResponseStoreDTO createStore(CreateStoreDTO createStoreDTO) {
        ifStoreAlreadyExist(createStoreDTO);

        Branch branch = branchEntityService.getEntityById(createStoreDTO.branchId());

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

    @Override
    public ResponseStoreDTO update(Long storeId, UpdateStoreDTO updateStoreDTO) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + storeId));

        if (updateStoreDTO.name() != null){
            String validatedName = nameValidatorService.validate(updateStoreDTO.name(), ObjectType.STORE);
            store.setName(validatedName);
        }

        storeMapper.updateStoreFromDTO(updateStoreDTO,store);

        updateBranchIfNeeded(updateStoreDTO, store);

        Store saved = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(saved);
    }

    @Override
    public boolean existsById(Long storeId){
        return storeRepository.existsById(storeId);
    }

    @Override
    public boolean existsByNameAndCode(StoreNameAndCodeDTO storeNameAndCodeDTO){
        return storeRepository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode());
    }

    @Override
    public void delete(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        if (!storeRepository.existsById(storeId)){
            throw new EntityNotFoundException("Store with id " + storeId +" does not exist");
        }

        storeRepository.deleteById(storeId);
    }

    @Override
    public ResponseStoreDTO save(Store store) {
        Store savedStore = storeRepository.save(store);

        return storeMapper.toResponseStoreDto(savedStore);
    }

    @Override
    public Store saveEntity(Store store) {
        return storeRepository.save(store);
    }

    @Override
    public Store getEntityById(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }


        return storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + storeId));
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

    private void updateBranchIfNeeded(UpdateStoreDTO updateStoreDTO,Store store) {
        if (updateStoreDTO.branchId() != null){
            Branch branch = branchEntityService.getEntityById(updateStoreDTO.branchId());

            store.setBranch(branch);
        }
    }
}
