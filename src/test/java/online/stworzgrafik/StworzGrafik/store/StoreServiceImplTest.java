package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
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
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private StoreRepository repository;

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

    private Region region = new TestRegionBuilder().build();
    private Branch branch = new TestBranchBuilder().withRegion(region).build();

    @Test
    void findAll_workingTest(){
        //given
        Store store1 = new TestStoreBuilder().withStoreCode("00").build();
        Store store2 = new TestStoreBuilder().withStoreCode("11").build();
        Store store3 = new TestStoreBuilder().withStoreCode("22").build();

        when(repository.findAll()).thenReturn(List.of(store1,store2,store3));

        ResponseStoreDTO responseOfStore1 = new TestResponseStoreDTO().buildFromEntity(store1);
        when(storeMapper.toResponseStoreDto(store1)).thenReturn(responseOfStore1);

        ResponseStoreDTO responseOfStore2 = new TestResponseStoreDTO().buildFromEntity(store2);
        when(storeMapper.toResponseStoreDto(store2)).thenReturn(responseOfStore2);

        ResponseStoreDTO responseOfStore3 = new TestResponseStoreDTO().buildFromEntity(store3);
        when(storeMapper.toResponseStoreDto(store3)).thenReturn(responseOfStore3);

        //when
        List<ResponseStoreDTO> responseDTOS = service.findAll();

        //then
        assertEquals(3,responseDTOS.size());
        assertTrue(responseDTOS.containsAll(List.of(responseOfStore1,responseOfStore2,responseOfStore3)));

        verify(repository,times(1)).findAll();
        verify(storeMapper,atLeastOnce()).toResponseStoreDto(any(Store.class));
    }

    @Test
    void findById_workingTest(){
        //given
        Long id = 1L;
        Store store = new TestStoreBuilder().build();

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().buildFromEntity(store);

        when(repository.findById(id)).thenReturn(Optional.of(store));

        when(storeMapper.toResponseStoreDto(store)).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO response = service.findById(id);

        //then
        assertEquals(store.getName(),response.name());
        assertEquals(store.getStoreCode(),response.storeCode());

        verify(repository,times(1)).findById(id);
        verify(storeMapper).toResponseStoreDto(any(Store.class));
    }

    @Test
    void createStore_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withBranch(branch).build();
        Long branchId = createStoreDTO.branchId();

        String name = createStoreDTO.name();
        when(repository.existsByName(name)).thenReturn(false);
        String storeCode = createStoreDTO.storeCode();
        when(repository.existsByStoreCode(storeCode)).thenReturn(false);

        when(branchEntityService.getEntityById(branchId)).thenReturn(branch);

        when(nameValidatorService.validate(name,ObjectType.STORE)).thenReturn(name);


        String location = createStoreDTO.location();
        Store store = new TestStoreBuilder()
                .withName(name)
                .withStoreCode(storeCode)
                .withLocation(location)
                .withBranch(branch)
                .build();

        when(storeBuilder.createStore(
                name,
                storeCode,
                location,
                branch
        )).thenReturn(store);

        when(repository.save(store)).thenReturn(store);

        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO()
                .withName(name)
                .withStoreCode(storeCode)
                .withBranch(branch)
                .withLocation(location)
                .build();

        when(storeMapper.toResponseStoreDto(store)).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO serviceResponse = service.createStore(createStoreDTO);

        //then
        assertEquals(name, serviceResponse.name());
        assertEquals(storeCode,serviceResponse.storeCode());
        assertEquals(location,serviceResponse.location());
        assertEquals(branch.getId(),serviceResponse.branchId());
    }

    @Test
    void createStore_argumentIsNull(){
        //given
        CreateStoreDTO createStoreDTO = null;

        //when
        assertThrows(NullPointerException.class,() -> service.createStore(createStoreDTO));

        //then
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void createStore_branchDoesNotExistThrowsException(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().build();

        when(repository.existsByName(createStoreDTO.name())).thenReturn(false);
        when(repository.existsByStoreCode(createStoreDTO.storeCode())).thenReturn(false);

        when(branchEntityService.getEntityById(createStoreDTO.branchId()))
                .thenThrow(new EntityNotFoundException("Cannot find branch by id " + createStoreDTO.branchId()));

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.createStore(createStoreDTO));

        //then
        assertEquals("Cannot find branch by id " + createStoreDTO.branchId(), exception.getMessage());
        verify(nameValidatorService,never()).validate(any(),any());
        verify(storeBuilder,never()).createStore(any(),any(),any(),any());
        verify(repository,never()).save(any());
        verify(storeMapper,never()).toResponseStoreDto(any());
    }

    @Test
    void exitsById_workingTest(){
        //given
        Long id = 1L;
        Long notExistingEntityId = 2L;

        when(repository.existsById(id)).thenReturn(true);
        when(repository.existsById(notExistingEntityId)).thenReturn(false);

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
        when(repository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode())).thenReturn(true);

        StoreNameAndCodeDTO notExistingStore = new StoreNameAndCodeDTO("Test","AA");
        when(repository.existsByNameAndStoreCode(notExistingStore.name(),notExistingStore.storeCode())).thenReturn(false);

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
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void delete_workingTest(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);

        //when
        service.delete(id);

        //then
        verify(repository,times(1)).deleteById(id);
    }

    @Test
    void delete_entityDoesNotExistById(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 200L;

        when(repository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.delete(id));

        //then
        assertEquals("Store with id " + id +" does not exist",exception.getMessage());
    }

    @Test
    void saveEntity_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        when(repository.save(store)).thenReturn(store);

        //when
        Store savedEntity = service.saveEntity(store);

        //then
        assertEquals(store.getName(),savedEntity.getName());
        assertEquals(store.getStoreCode(),savedEntity.getStoreCode());
        assertEquals(store.getLocation(),savedEntity.getLocation());
        assertEquals(store.getBranch(),savedEntity.getBranch());

        verify(repository,times(1)).save(store);
    }

    @Test
    void save_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();

        when(repository.save(store)).thenReturn(store);

        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().buildFromEntity(store);
        when (storeMapper.toResponseStoreDto(store)).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO returnedDto = service.save(store);

        //then
        assertEquals(store.getId(),returnedDto.id());
        assertEquals(store.getLocation(),returnedDto.location());

        verify(repository,times(1)).save(any(Store.class));
    }

    @Test
    void update_workingTest(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 1L;
        Store store = new TestStoreBuilder().build();
        when(repository.findById(id)).thenReturn(Optional.of(store));

        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

        when(nameValidatorService.validate(updateStoreDTO.name(), ObjectType.STORE)).thenReturn(updateStoreDTO.name());

        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().buildFromEntity(store);

        when(repository.findById(id)).thenReturn(Optional.of(store));
        when(repository.save(any(Store.class))).thenReturn(store);
        when(storeMapper.toResponseStoreDto(any(Store.class))).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO updated = service.update(id, updateStoreDTO);

        //then
        assertEquals(responseStoreDTO,updated);

        verify(repository,times(1)).findById(id);
        verify(storeMapper,times(1)).toResponseStoreDto(store);
        verify(repository,times(1)).save(any(Store.class));
    }

    @Test
    void update_entityNotFoundByIdThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);

        Long id = 100L;
        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

        when(repository.findById(id)).thenThrow(EntityNotFoundException.class);

        //when
        assertThrows(EntityNotFoundException.class,() -> service.update(id,updateStoreDTO));

        //then
        verify(repository,times(1)).findById(any(Long.class));
        verify(repository,never()).save(any(Store.class));
    }
}
