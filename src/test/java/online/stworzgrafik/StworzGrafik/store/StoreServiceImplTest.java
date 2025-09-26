package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.*;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private BranchRepository branchRepository;

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
    void findById_idIsNull(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findById(id));

        //then
        assertEquals("Id cannot be null",exception.getMessage());

        verify(repository,never()).findById(any());
        verify(storeMapper,never()).toResponseStoreDto(any());
    }

    @Test
    void create_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().build();

        Branch branch = new TestBranchBuilder().build();
        when(branchRepository.findById(createStoreDTO.branchId())).thenReturn(Optional.of(branch));

        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO(createStoreDTO.name(), createStoreDTO.storeCode());
        when(storeMapper.toStoreNameAndCodeDTO(createStoreDTO)).thenReturn(storeNameAndCodeDTO);

        Store store = new TestStoreBuilder().build();

        ResponseStoreDTO responseStoreDTO = new ResponseStoreDTO(
                1L,
                createStoreDTO.name(),
                createStoreDTO.storeCode(),
                createStoreDTO.location(),
                createStoreDTO.branchId(),
                "responseName",
                createStoreDTO.region(),
                LocalDateTime.now(),
                true,
                1L,
                createStoreDTO.openForClientsHour(),
                createStoreDTO.closeForClientsHour()
        );
        when(storeMapper.toResponseStoreDto(any(Store.class))).thenReturn(responseStoreDTO);

        when(storeBuilder.createStore(
                createStoreDTO.name(),
                createStoreDTO.storeCode(),
                createStoreDTO.location(),
                branch,
                createStoreDTO.region(),
                createStoreDTO.openForClientsHour(),
                createStoreDTO.closeForClientsHour())).thenReturn(store);
        when(repository.save(any(Store.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(repository.existsByNameAndStoreCode(any(),any())).thenReturn(false);

        //when
        ResponseStoreDTO serviceReturn = service.create(createStoreDTO);

        //then
        assertEquals(serviceReturn.name(),createStoreDTO.name());
        assertEquals(serviceReturn.storeCode(),createStoreDTO.storeCode());
        assertEquals(serviceReturn.location(),createStoreDTO.location());
        assertEquals(serviceReturn.branchId(), createStoreDTO.branchId());
        assertEquals(serviceReturn.region(),createStoreDTO.region());
        assertEquals(serviceReturn.openForClientsHour(), createStoreDTO.openForClientsHour());
        assertEquals(serviceReturn.closeForClientsHour(),createStoreDTO.closeForClientsHour());

        verify(repository,times(1)).save(any(Store.class));
        }

    @Test
    void create_argumentIsNull(){
        //given
        CreateStoreDTO createStoreDTO = null;

        //when
        assertThrows(NullPointerException.class,() -> service.create(createStoreDTO));

        //then
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void create_closeHourIsBeforeOpenHour(){
        //given
        CreateStoreDTO inputDto = new TestCreateStoreDTO().build();
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO(inputDto.name(),inputDto.storeCode());

        when(storeMapper.toStoreNameAndCodeDTO(inputDto)).thenReturn(storeNameAndCodeDTO);
        when(repository.existsByNameAndStoreCode(inputDto.name(),inputDto.storeCode())).thenReturn(true);

        //when
        assertThrows(EntityExistsException.class,() -> service.create(inputDto));

        //then
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void exitsById_workingTest(){
        //given
        Long id = 1L;
        Long notExistingEntityId = 2L;

        when(repository.existsById(id)).thenReturn(true);
        when(repository.existsById(notExistingEntityId)).thenReturn(false);

        //when
        boolean exists = service.exists(id);
        boolean shouldNotExist = service.exists(notExistingEntityId);

        //then
        assertTrue(exists);
        assertFalse(shouldNotExist);
    }

    @Test
    void existsById_idIsNull(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());
    }

    @Test
    void existsByStoreNameAndStoreCode_workingTest(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO("Best store","00");
        when(repository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode())).thenReturn(true);

        StoreNameAndCodeDTO notExistingStore = new StoreNameAndCodeDTO("Test","AA");
        when(repository.existsByNameAndStoreCode(notExistingStore.name(),notExistingStore.storeCode())).thenReturn(false);

        //when
        boolean exists = service.exists(storeNameAndCodeDTO);
        boolean shouldNotExist = service.exists(notExistingStore);

        //then
        assertTrue(exists);
        assertFalse(shouldNotExist);
    }

    @Test
    void existsByStoreNameAndStoreCode_argumentIsNull(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> service.exists(storeNameAndCodeDTO));

        //then
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void delete_workingTest(){
        //given
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);

        //when
        service.delete(id);

        //then
        verify(repository,times(1)).deleteById(id);
    }

    @Test
    void delete_idIsNull(){
        //given
        Long id = null;

        //when
        assertThrows(NullPointerException.class,() -> service.delete(id));

        //then
        verify(repository,never()).deleteById(any());
    }

    @Test
    void delete_entityDoesNotExistById(){
        //given
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
        assertEquals(store.getBranch(),savedEntity.getBranch());
        assertEquals(store.getOpenForClientsHour(),savedEntity.getOpenForClientsHour());
        assertEquals(store.getCloseForClientsHour(),savedEntity.getCloseForClientsHour());

        verify(repository,times(1)).save(store);
    }

    @Test
    void saveEntity_argumentIsNull(){
        //given
        Store store = null;

        //when
        assertThrows(NullPointerException.class,() -> service.saveEntity(store));

        //then
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void saveDto_workingTest(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO("Name","a1");
        Store entityFromDTO = new TestStoreBuilder().build();
        entityFromDTO.setName(storeNameAndCodeDTO.name());
        entityFromDTO.setStoreCode(storeNameAndCodeDTO.storeCode());

        when(storeMapper.toEntity(storeNameAndCodeDTO)).thenReturn(entityFromDTO);

        entityFromDTO.setName("New name before save");
        when(repository.save(entityFromDTO)).thenReturn(entityFromDTO);

        ResponseStoreDTO responseStoreDTO = new TestResponseStoreDTO().buildFromEntity(entityFromDTO);
        when(storeMapper.toResponseStoreDto(entityFromDTO)).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO returnedDto = service.saveDto(storeNameAndCodeDTO);

        //then
        assertEquals("New name before save",returnedDto.name());
        assertEquals(entityFromDTO.getId(),returnedDto.id());
        assertEquals(entityFromDTO.getLocation(),returnedDto.location());

        verify(repository,times(1)).save(any(Store.class));
    }



    @Test
    void saveDto_argumentIsNull(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = null;

        //when
        assertThrows(NullPointerException.class,() -> service.saveDto(storeNameAndCodeDTO));

        //then
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void update_workingTest(){
        //given
        Long id = 1L;
        Store store = new TestStoreBuilder().build();
        when(repository.findById(id)).thenReturn(Optional.of(store));

        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

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
    void update_idIsNull(){
        //given
        Long id = null;
        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.update(id, updateStoreDTO));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(repository,never()).findById(any(Long.class));
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void update_dtoIsNull(){
        Long id = 90L;
        UpdateStoreDTO updateStoreDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> service.update(id, updateStoreDTO));

        //then
        verify(repository,never()).findById(any(Long.class));
        verify(repository,never()).save(any(Store.class));
    }

    @Test
    void update_entityNotFoundByIdThrowsException(){
        //given
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
