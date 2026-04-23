package online.stworzgrafik.StworzGrafik.store;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.DTO.*;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.openingHours.StoreOpeningHoursService;
import online.stworzgrafik.StworzGrafik.store.storeDetails.StoreDetails;
import online.stworzgrafik.StworzGrafik.store.storeDetails.StoreDetailsService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final StoreOpeningHoursService openingHoursService;
    private final StoreDeliveryService storeDeliveryService;
    private final StoreDetailsService storeDetailsService;

    @Override
    public Page<ResponseStoreDTO> findAll(Pageable pageable) {
        return storeRepository.findAll(pageable)
                .map(storeMapper::toResponseStoreDto);
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
    public Page<ResponseStoreDTO> findByCriteria(StoreSpecificationDTO dto, Pageable pageable) {
        if (dto == null) return Page.empty();

        Specification<Store> specification = Specification.allOf(
                StoreSpecification.hasStoreCode(dto.storeCode()),
                StoreSpecification.hasNameLike(dto.name()),
                StoreSpecification.hasLocationLike(dto.location()),
                StoreSpecification.hasBranchId(dto.branchId()),
                StoreSpecification.hasStoreManagerId(dto.storeManagerId()),
                StoreSpecification.isEnable(dto.enable())
        );

        return storeRepository.findAll(specification,pageable)
                .map(storeMapper::toResponseStoreDto);
    }

    @Override
    public ResponseStoreDTO createStore(CreateStoreDTO createStoreDTO) {
        return storeMapper.toResponseStoreDto(createEntityStore(createStoreDTO));
    }

    @Override
    public ResponseStoreDTO update(Long storeId, UpdateStoreDTO updateStoreDTO) {
        return storeMapper.toResponseStoreDto(updateEntityStore(storeId,updateStoreDTO));
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
    public Store createEntityStore(CreateStoreDTO dto) {
        ifStoreAlreadyExist(dto);

        Branch branch = branchEntityService.getEntityById(dto.branchId());

        String validatedName = nameValidatorService.validate(dto.name(), ObjectType.STORE);

        Store store = storeBuilder.createStore(
                validatedName,
                dto.storeCode(),
                dto.location(),
                branch
        );

        Store savedStore = storeRepository.save(store);

        storeDetailsService.initializeDefault(savedStore);
        storeDeliveryService.initializeDefault(savedStore);
        openingHoursService.initializeDefaultHours(savedStore);

        return savedStore;
    }

    @Override
    public Store updateEntityStore(Long storeId, UpdateStoreDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + storeId));

        if (dto.name() != null){
            String validatedName = nameValidatorService.validate(dto.name(), ObjectType.STORE);
            store.setName(validatedName);
        }

        storeMapper.updateStoreFromDTO(dto,store);

        updateBranchIfNeeded(dto, store);

        return storeRepository.save(store);
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
