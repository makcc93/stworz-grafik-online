package online.stworzgrafik.StworzGrafik.store;

import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class StoreMapperTest {

    private final StoreMapper mapper = new StoreMapperImpl();

    @Test
    void toResponseStoreDto_workingTest(){
        //given
        Store store = getStore();

        //when
        ResponseStoreDTO responseStoreDto = mapper.toResponseStoreDto(store);

        //then
        assertEquals(responseStoreDto.id(),store.getId());
        assertEquals(responseStoreDto.storeCode(),store.getStoreCode());
        assertEquals(responseStoreDto.location(),store.getLocation());
        assertEquals(responseStoreDto.region(),store.getRegion());
        assertEquals(responseStoreDto.branchId(),store.getBranch().getId());
        assertEquals(responseStoreDto.openForClientsHour(),store.getOpenForClientsHour());
        assertEquals(responseStoreDto.closeForClientsHour(),store.getCloseForClientsHour());
    }

    @Test
    void toStoreNameAndCodeDTO_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = getCreateStoreDTO();

        //when
        StoreNameAndCodeDTO storeNameAndCodeDTO = mapper.toStoreNameAndCodeDTO(createStoreDTO);

        //then
        assertEquals(storeNameAndCodeDTO.name(),createStoreDTO.name());
        assertEquals(storeNameAndCodeDTO.storeCode(),createStoreDTO.storeCode());
    }

    @Test
    void toEntityFromCreateStoreDTO_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = getCreateStoreDTO();

        //when
        Store entity = mapper.toEntity(createStoreDTO);

        //then
        assertEquals(entity.getName(),createStoreDTO.name());
        assertEquals(entity.getStoreCode(),createStoreDTO.storeCode());
        assertEquals(entity.getLocation(),createStoreDTO.location());
        assertEquals(entity.getRegion(),createStoreDTO.region());
        assertEquals(entity.getOpenForClientsHour(),createStoreDTO.openForClientsHour());
        assertEquals(entity.getCloseForClientsHour(),createStoreDTO.closeForClientsHour());
    }

    @Test
    void toEntityFromStoreNameAndCodeDTO_workingTest(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = getStoreNameAndCodeDTO();

        //when
        Store entity = mapper.toEntity(storeNameAndCodeDTO);

        //then
        assertEquals(entity.getName(),storeNameAndCodeDTO.name());
        assertEquals(entity.getStoreCode(),storeNameAndCodeDTO.storeCode());
    }

    @Test
    void updateStoreFromDTO_workingTest(){
        //given
        Store store = getStore();
        UpdateStoreDTO updateStoreDTO = getUpdateStoreDTO();

        //when
        mapper.updateStoreFromDTO(updateStoreDTO,store);

        //then
        assertEquals(updateStoreDTO.name(),store.getName());
        assertEquals(updateStoreDTO.storeCode(),store.getStoreCode());
        assertEquals(updateStoreDTO.location(),store.getLocation());
        assertEquals(updateStoreDTO.region(),store.getRegion());
        assertEquals(updateStoreDTO.openForClientsHour(),store.getOpenForClientsHour());
        assertEquals(updateStoreDTO.closeForClientsHour(),store.getCloseForClientsHour());
    }

    private UpdateStoreDTO getUpdateStoreDTO() {
        return new UpdateStoreDTO(
                "Test",
                "TE",
                "Wroclaw",
                1L,
                RegionType.POLUDNIE,
                true,
                1337L,
                LocalTime.of(10,0),
                LocalTime.of(20,0)
        );
    }

    private StoreNameAndCodeDTO getStoreNameAndCodeDTO(){
        return new StoreNameAndCodeDTO(
            "Simple Name",
            "SN");

    }

    private CreateStoreDTO getCreateStoreDTO(){
        return new CreateStoreDTO(
                "DTOname",
                "TT",
                "Map City",
                1L,
                RegionType.POLUDNIE,
                LocalTime.of(9,0),
                LocalTime.of(21,0)
        );
    }

    private Store getStore(){
        return new StoreBuilder().createStore(
                "TestName",
                "11",
                "Lublin",
                new BranchBuilder().createBranch("randomName"),
                RegionType.WSCHOD,
                LocalTime.of(10,0),
                LocalTime.of(19,0)
        );
    }
}