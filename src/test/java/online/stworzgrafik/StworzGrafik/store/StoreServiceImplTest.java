package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseDetailStoreDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        ResponseDetailStoreDTO responseDetailStoreDTO = ResponseDetailStoreDTO.from(store);

        when(storeBuilder.createStore(inputDto.name(),
                inputDto.storeCode(),
                inputDto.location(),
                inputDto.branch(),
                inputDto.region(),
                inputDto.openForClientsHour(),
                inputDto.closeForClientsHour())).thenReturn(store);
        when(repository.save(any(Store.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(storeMapper.toDetailStoreDto(any(Store.class))).thenReturn(responseDetailStoreDTO);

        //when
        ResponseDetailStoreDTO serviceReturn = service.create(inputDto);

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
        when(repository.existsByNameAndStoreCode(inputDto.name(),inputDto.storeCode())).thenReturn(true);

        //when
        assertThrows(IllegalArgumentException.class,() -> service.create(inputDto));
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
