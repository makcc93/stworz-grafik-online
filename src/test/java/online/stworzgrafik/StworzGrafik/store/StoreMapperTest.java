package online.stworzgrafik.StworzGrafik.store;

import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestCreateStoreDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestUpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class StoreMapperTest {

    private final StoreMapper mapper = new StoreMapperImpl();

    @Test
    void toResponseStoreDto_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();

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
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().build();

        //when
        StoreNameAndCodeDTO storeNameAndCodeDTO = mapper.toStoreNameAndCodeDTO(createStoreDTO);

        //then
        assertEquals(storeNameAndCodeDTO.name(),createStoreDTO.name());
        assertEquals(storeNameAndCodeDTO.storeCode(),createStoreDTO.storeCode());
    }

    @Test
    void toEntityFromCreateStoreDTO_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().build();

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
        Store store = new TestStoreBuilder().build();
        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

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

    private StoreNameAndCodeDTO getStoreNameAndCodeDTO(){
        return new StoreNameAndCodeDTO(
            "Simple Name",
            "SN");

    }
}