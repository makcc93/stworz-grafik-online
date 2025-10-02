package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestCreateRegionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestUpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.method.AuthorizeReturnObject;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RegionServiceImplIT {
    @Autowired
    private RegionService regionService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private RegionBuilder regionBuilder;


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
        assertThrows(EntityExistsException.class,() -> regionService.createRegion(createRegionDTO),
        "Region with name " + usedName + " already exist");

        //then
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

    //time to add another IT tests

}
