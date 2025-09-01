package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void create_workingTest(){
        //given
        CreateStoreDTO inputDto = getCreateStoreDTO();

        Store store = getStore(inputDto);
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO("Name","A1");
        when(storeMapper.toStoreNameAndCode(inputDto)).thenReturn(storeNameAndCodeDTO);

        ResponseStoreDTO responseStoreDTO = ResponseStoreDTO.from(store);

        when(storeBuilder.createStore(inputDto.name(),
                inputDto.storeCode(),
                inputDto.location(),
                inputDto.branch(),
                inputDto.region(),
                inputDto.openForClientsHour(),
                inputDto.closeForClientsHour())).thenReturn(store);
        when(repository.save(any(Store.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(storeMapper.toResponseStoreDto(any(Store.class))).thenReturn(responseStoreDTO);

        //when
        ResponseStoreDTO serviceReturn = service.create(inputDto);

        //then

        assertEquals(serviceReturn.name(),inputDto.name());
        assertEquals(serviceReturn.storeCode(),inputDto.storeCode());
        assertEquals(serviceReturn.location(),inputDto.location());
        assertEquals(serviceReturn.branch(), inputDto.branch());
        assertEquals(serviceReturn.region(),inputDto.region());
        assertEquals(serviceReturn.openForClientsHour(), inputDto.openForClientsHour());
        assertEquals(serviceReturn.closeForClientsHour(),inputDto.closeForClientsHour());

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
        CreateStoreDTO inputDto = getCreateStoreDTO();
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO(inputDto.name(),inputDto.storeCode());

        when(storeMapper.toStoreNameAndCode(inputDto)).thenReturn(storeNameAndCodeDTO);
        when(repository.existsByNameAndStoreCode(inputDto.name(),inputDto.storeCode())).thenReturn(true);

        //when
        assertThrows(IllegalArgumentException.class,() -> service.create(inputDto));

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
    void saveEntity_workingTest(){
        //given
        Store store = getStore();
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
        Store entityFromDTO = getStore();
        entityFromDTO.setName(storeNameAndCodeDTO.name());
        entityFromDTO.setStoreCode(storeNameAndCodeDTO.storeCode());

        when(storeMapper.toEntity(storeNameAndCodeDTO)).thenReturn(entityFromDTO);

        entityFromDTO.setName("New name before save");
        when(repository.save(entityFromDTO)).thenReturn(entityFromDTO);

        ResponseStoreDTO responseStoreDTO = new ResponseStoreDTO(
                entityFromDTO.getId(),
                entityFromDTO.getName(),
                entityFromDTO.getStoreCode(),
                entityFromDTO.getLocation(),
                entityFromDTO.getBranch(),
                entityFromDTO.getRegion(),
                LocalDateTime.now(),
                true,
                1L,
                entityFromDTO.getOpenForClientsHour(),
                entityFromDTO.getCloseForClientsHour()
        );
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

    private static Store getStore(CreateStoreDTO inputDto) {
        Store store = new Store();
        store.setName(inputDto.name());
        store.setStoreCode(inputDto.storeCode());
        store.setLocation(inputDto.location());
        store.setBranch(inputDto.branch());
        store.setRegion(inputDto.region());
        store.setOpenForClientsHour(inputDto.openForClientsHour());
        store.setCloseForClientsHour(inputDto.closeForClientsHour());

        return store;
    }

    private static Store getStore(){
        Store store = new Store();
        store.setName("TestName");
        store.setStoreCode("AB");
        store.setLocation("TestLocation");
        store.setBranch(BranchType.GDANSK);
        store.setRegion(RegionType.ZACHOD);
        store.setOpenForClientsHour(LocalTime.of(10,0));
        store.setCloseForClientsHour(LocalTime.of(20,0));

        return store;
    }

    private static CreateStoreDTO getCreateStoreDTO() {
        return new CreateStoreDTO(
                "Test store",
                "01",
                "Testcity",
                BranchType.GDANSK,
                RegionType.ZACHOD,
                LocalTime.of(10,0),
                LocalTime.of(21,0)
        );
    }
}
