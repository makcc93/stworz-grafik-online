package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RegionServiceIT {
    @Autowired
    private RegionService regionService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionBuilder regionBuilder;

    @Autowired
    private RegionMapper regionMapper;


    @Test
    void createRegion_workingTest(){
        //given
        String name = "NEW";
        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName(name).build();

        //when
        ResponseRegionDTO serviceResponse = regionService.createRegion(createRegionDTO);

        //then
        assertEquals(name, serviceResponse.name());
        assertTrue(regionRepository.existsByName(name));
    }

    @Test
    void createRegion_entityWithThisNameAlreadyExistThrowsException(){
        //given
        String usedName = "NAME";
        Region region = regionBuilder.createRegion(usedName);
        regionRepository.save(region);

        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName(usedName).build();

        //when
        EntityExistsException exception = assertThrows(EntityExistsException.class, () -> regionService.createRegion(createRegionDTO));

        //then
        assertEquals("Region with name " + usedName + " already exist", exception.getMessage());
    }

    @Test
    void updateRegion_workingTest(){
        //given
        String name = "NAME";
        Region region = regionBuilder.createRegion(name);
        region.setEnable(true);
        regionRepository.save(region);

        String newName = "NEWNAME";
        boolean newEnable = false;
        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().withName(newName).withIsEnable(newEnable).build();

        //when
       regionService.updateRegion(region.getId(), updateRegionDTO);

        //then
        assertEquals(newName,region.getName());
        assertFalse(region.isEnable());
    }

    @Test
    void updateRegion_entityDoesNotExistThrowsException(){
        //given
        Long notExistingId = 1234L;
        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> regionService.updateRegion(notExistingId, updateRegionDTO));

        //then
        assertEquals("Cannot find region by id " + notExistingId, exception.getMessage());
    }

    @Test
    void findAll_workingTest(){
        //given
        Region region1 = new TestRegionBuilder().withName("FIRST").build();
        Region region2 = new TestRegionBuilder().withName("SECOND").build();
        Region region3 = new TestRegionBuilder().withName("THIRD").build();
        regionRepository.saveAll(List.of(region1,region2,region3));

        ResponseRegionDTO responseRegionDTO1 = regionMapper.toResponseRegionDTO(region1);
        ResponseRegionDTO responseRegionDTO2 = regionMapper.toResponseRegionDTO(region2);
        ResponseRegionDTO responseRegionDTO3 = regionMapper.toResponseRegionDTO(region3);

        //when
        List<ResponseRegionDTO> serviceResponse = regionService.findAll();

        //then
        assertEquals(3,serviceResponse.size());
        assertTrue(serviceResponse.containsAll(List.of(responseRegionDTO1,responseRegionDTO2,responseRegionDTO3)));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponseRegionDTO> emptyServiceResponse = regionService.findAll();

        //then
        assertEquals(0,emptyServiceResponse.size());
        assertTrue(emptyServiceResponse.isEmpty());
    }

    @Test
    void findById_workingTest(){
        //given
        String name = "SECOND";

        Region region1 = new TestRegionBuilder().withName("FIRST").build();
        Region region2 = new TestRegionBuilder().withName(name).build();
        Region region3 = new TestRegionBuilder().withName("THIRD").build();
        regionRepository.saveAll(List.of(region1,region2,region3));

        //when
        ResponseRegionDTO serviceResponse = regionService.findById(region2.getId());

        //then
        assertEquals(name,serviceResponse.name());
        assertTrue(regionRepository.existsById(region2.getId()));
    }

    @Test
    void findById_entityDoesNotExistThrowsException(){
        //given
        Long notExistingEntityId = 123123L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> regionService.findById(notExistingEntityId));

        //then
        assertEquals("Cannot find region by id " + notExistingEntityId, exception.getMessage());
    }

    @Test
    void existsById_workingTest(){
        //given
        Region region = new TestRegionBuilder().build();
        regionRepository.save(region);

        //when
        boolean response = regionService.exists(region.getId());

        //then
        assertTrue(response);
    }

    @Test
    void existsById_notFoundEntityDoesNotThrowException(){
        //given
        long notExistingEntityId = 421421L;

        //when
        boolean response = assertDoesNotThrow(() -> regionService.exists(notExistingEntityId));

        //then
        assertFalse(response);
    }

    @Test
    void deleteRegion_workingTest(){
        //given
        Region region1 = new TestRegionBuilder().withName("FIRST").build();
        regionRepository.save(region1);

        Region region2 = new TestRegionBuilder().withName("SECOND").build();
        regionRepository.save(region2);

        //when
        regionService.deleteRegion(region1.getId());

        //then
        assertFalse(regionRepository.existsById(region1.getId()));
        assertTrue(regionRepository.existsById(region2.getId()));
    }

    @Test
    void deleteRegion_entityDoesNotExistThrowsException(){
        //given
        long notExistingEntityId = 531541L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> regionService.deleteRegion(notExistingEntityId));

        //then
        assertEquals("Cannot find region by id " + notExistingEntityId, exception.getMessage());
    }
}
