package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.openingHours.StoreOpeningHoursService;
import online.stworzgrafik.StworzGrafik.store.storeDetails.StoreDetailsService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @InjectMocks
    private StoreServiceImpl service;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreBuilder storeBuilder;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private BranchService branchService;

    @Mock
    private BranchEntityService branchEntityService;

    @Mock
    private NameValidatorService nameValidatorService;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock private StoreDetailsService storeDetailsService;
    @Mock private StoreDeliveryService storeDeliveryService;
    @Mock private StoreOpeningHoursService openingHoursService;

    private Region region = new TestRegionBuilder().build();
    private Branch branch = new TestBranchBuilder().withRegion(region).build();

    @Test
    void createEntityStore_whenValidData_shouldInitializeAllAndReturnStore() {
        // given
        CreateStoreDTO dto = new TestCreateStoreDTO().withBranch(branch).build();

        String name      = dto.name();
        String storeCode = dto.storeCode();
        String location  = dto.location();

        // ifStoreAlreadyExist sprawdza te dwa
        when(storeRepository.existsByName(name)).thenReturn(false);
        when(storeRepository.existsByStoreCode(storeCode)).thenReturn(false);

        when(branchEntityService.getEntityById(dto.branchId())).thenReturn(branch);
        when(nameValidatorService.validate(name, ObjectType.STORE)).thenReturn(name);

        Store builtStore = new TestStoreBuilder()
                .withName(name).withStoreCode(storeCode)
                .withLocation(location).withBranch(branch)
                .build();

        when(storeBuilder.createStore(name, storeCode, location, branch))
                .thenReturn(builtStore);

        // save zwraca ten sam obiekt — savedStore == builtStore
        when(storeRepository.save(builtStore)).thenReturn(builtStore);

        // serwisy void — Mockito domyślnie nic nie robi, ale @Mock MUSI istnieć
        // doNothing() jest zbędne, pokazuję tylko dla czytelności:
        // doNothing().when(storeDetailsService).initializeDefault(builtStore);

        // when
        Store result = service.createEntityStore(dto);

        // then — wynik
        assertNotNull(result);
        assertEquals(name,      result.getName());
        assertEquals(storeCode, result.getStoreCode());
        assertEquals(location,  result.getLocation());
        assertEquals(branch,    result.getBranch());

        // then — weryfikacja wywołań serwisów inicjalizujących
        verify(storeDetailsService).initializeDefault(builtStore);
        verify(storeDeliveryService).initializeDefault(builtStore);
        verify(openingHoursService).initializeDefaultHours(builtStore);

        // save wywołany dokładnie raz
        verify(storeRepository).save(builtStore);
    }

    @Test
    void createEntityStore_whenNameAlreadyExists_shouldThrow() {
        CreateStoreDTO dto = new TestCreateStoreDTO().withBranch(branch).build();
        when(storeRepository.existsByName(dto.name())).thenReturn(true);

        assertThrows(EntityExistsException.class,
                () -> service.createEntityStore(dto));

        // żaden serwis inicjalizujący nie może zostać wywołany
        verify(storeRepository, never()).save(any());
        verifyNoInteractions(storeDetailsService, storeDeliveryService, openingHoursService);
    }

    @Test
    void createEntityStore_whenStoreCodeAlreadyExists_shouldThrow() {
        CreateStoreDTO dto = new TestCreateStoreDTO().withBranch(branch).build();
        when(storeRepository.existsByName(dto.name())).thenReturn(false);
        when(storeRepository.existsByStoreCode(dto.storeCode())).thenReturn(true);

        assertThrows(EntityExistsException.class,
                () -> service.createEntityStore(dto));

        verify(storeRepository, never()).save(any());
    }

    @Test
    void findAll_workingTest(){
        //given
        Pageable pageable = PageRequest.of(0,50);

        Store store1 = new TestStoreBuilder().withStoreCode("00").build();
        Store store2 = new TestStoreBuilder().withStoreCode("11").build();
        Store store3 = new TestStoreBuilder().withStoreCode("22").build();
        List<Store> stores = List.of(store1, store2, store3);
        PageImpl<Store> storesPage = new PageImpl<>(stores, pageable, stores.size());

        when(storeRepository.findAll(pageable)).thenReturn(storesPage);

        ResponseStoreDTO responseOfStore1 = new TestResponseStoreDTO().withStoreCode("00").build();
        when(storeMapper.toResponseStoreDto(store1)).thenReturn(responseOfStore1);

        ResponseStoreDTO responseOfStore2 = new TestResponseStoreDTO().withStoreCode("11").build();
        when(storeMapper.toResponseStoreDto(store2)).thenReturn(responseOfStore2);

        ResponseStoreDTO responseOfStore3 = new TestResponseStoreDTO().withStoreCode("22").build();
        when(storeMapper.toResponseStoreDto(store3)).thenReturn(responseOfStore3);

        //when
        Page<ResponseStoreDTO> responseDTOS = service.findAll(pageable);

        //then
        assertEquals(3,responseDTOS.getContent().size());
        assertTrue(responseDTOS.getContent().containsAll(List.of(responseOfStore1,responseOfStore2,responseOfStore3)));

        verify(storeRepository,times(1)).findAll(pageable);
        verify(storeMapper,atLeastOnce()).toResponseStoreDto(any(Store.class));
    }

    @Test
    void findById_workingTest(){
        //given
        Long id = 1L;
        Store store = new TestStoreBuilder().build();

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().withName(store.getName()).build();

        when(storeRepository.findById(id)).thenReturn(Optional.of(store));

        when(storeMapper.toResponseStoreDto(store)).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO response = service.findById(id);

        //then
        assertEquals(responseStoreDTO.name(),response.name());
        assertEquals(responseStoreDTO.storeCode(),response.storeCode());

        verify(storeRepository,times(1)).findById(id);
        verify(storeMapper).toResponseStoreDto(any(Store.class));
    }

    @Test
    void createStore_argumentIsNull(){
        //given
        CreateStoreDTO createStoreDTO = null;

        //when
        assertThrows(NullPointerException.class,() -> service.createStore(createStoreDTO));

        //then
        verify(storeRepository,never()).save(any(Store.class));
    }

    @Test
    void createStore_branchDoesNotExistThrowsException(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().build();

        when(storeRepository.existsByName(createStoreDTO.name())).thenReturn(false);
        when(storeRepository.existsByStoreCode(createStoreDTO.storeCode())).thenReturn(false);

        when(branchEntityService.getEntityById(createStoreDTO.branchId()))
                .thenThrow(new EntityNotFoundException("Cannot find branch by id " + createStoreDTO.branchId()));

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.createStore(createStoreDTO));

        //then
        assertEquals("Cannot find branch by id " + createStoreDTO.branchId(), exception.getMessage());
        verify(nameValidatorService,never()).validate(any(),any());
        verify(storeBuilder,never()).createStore(any(),any(),any(),any());
        verify(storeRepository,never()).save(any());
        verify(storeMapper,never()).toResponseStoreDto(any());
    }

    @Test
    void exitsById_workingTest(){
        //given
        Long id = 1L;
        Long notExistingEntityId = 2L;

        when(storeRepository.existsById(id)).thenReturn(true);
        when(storeRepository.existsById(notExistingEntityId)).thenReturn(false);

        //when
        boolean exists = service.existsById(id);
        boolean shouldNotExist = service.existsById(notExistingEntityId);

        //then
        assertTrue(exists);
        assertFalse(shouldNotExist);
    }

    @Test
    void existsByStoreNameAndStoreCode_workingTest(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO("Best store","00");
        when(storeRepository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode())).thenReturn(true);

        StoreNameAndCodeDTO notExistingStore = new StoreNameAndCodeDTO("Test","AA");
        when(storeRepository.existsByNameAndStoreCode(notExistingStore.name(),notExistingStore.storeCode())).thenReturn(false);

        //when
        boolean exists = service.existsByNameAndCode(storeNameAndCodeDTO);
        boolean shouldNotExist = service.existsByNameAndCode(notExistingStore);

        //then
        assertTrue(exists);
        assertFalse(shouldNotExist);
    }

    @Test
    void existsByStoreNameAndStoreCode_argumentIsNull(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> service.existsByNameAndCode(storeNameAndCodeDTO));

        //then
        verify(storeRepository,never()).save(any(Store.class));
    }

    @Test
    void delete_workingTest(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 1L;
        when(storeRepository.existsById(id)).thenReturn(true);

        //when
        service.delete(id);

        //then
        verify(storeRepository,times(1)).deleteById(id);
    }

    @Test
    void delete_entityDoesNotExistById(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 200L;

        when(storeRepository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.delete(id));

        //then
        assertEquals("Store with id " + id +" does not exist",exception.getMessage());
    }

    @Test
    void saveEntity_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        when(storeRepository.save(store)).thenReturn(store);

        //when
        Store savedEntity = service.saveEntity(store);

        //then
        assertEquals(store.getName(),savedEntity.getName());
        assertEquals(store.getStoreCode(),savedEntity.getStoreCode());
        assertEquals(store.getLocation(),savedEntity.getLocation());
        assertEquals(store.getBranch(),savedEntity.getBranch());

        verify(storeRepository,times(1)).save(store);
    }

    @Test
    void save_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();

        when(storeRepository.save(store)).thenReturn(store);

        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().build();
        when (storeMapper.toResponseStoreDto(store)).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO returnedDto = service.save(store);

        //then
        assertEquals(responseStoreDTO.id(),returnedDto.id());
        assertEquals(responseStoreDTO.location(),returnedDto.location());

        verify(storeRepository,times(1)).save(any(Store.class));
    }

    @Test
    void update_workingTest(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 1L;
        Store store = new TestStoreBuilder().build();
        when(storeRepository.findById(id)).thenReturn(Optional.of(store));

        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

        when(nameValidatorService.validate(updateStoreDTO.name(), ObjectType.STORE)).thenReturn(updateStoreDTO.name());

        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().build();

        when(storeRepository.findById(id)).thenReturn(Optional.of(store));
        when(storeRepository.save(any(Store.class))).thenReturn(store);
        when(storeMapper.toResponseStoreDto(any(Store.class))).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO updated = service.update(id, updateStoreDTO);

        //then
        assertEquals(responseStoreDTO,updated);

        verify(storeRepository,times(1)).findById(id);
        verify(storeMapper,times(1)).toResponseStoreDto(store);
        verify(storeRepository,times(1)).save(any(Store.class));
    }

    @Test
    void update_entityNotFoundByIdThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 100L;
        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

        when(storeRepository.findById(id)).thenThrow(EntityNotFoundException.class);

        //when
        assertThrows(EntityNotFoundException.class,() -> service.update(id,updateStoreDTO));

        //then
        verify(storeRepository,times(1)).findById(any(Long.class));
        verify(storeRepository,never()).save(any(Store.class));
    }
}
