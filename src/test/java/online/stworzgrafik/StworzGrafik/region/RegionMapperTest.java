package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestUpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegionMapperTest {
    @InjectMocks
    RegionMapperImpl regionMapper;

    @Test
    void toResponseRegionDTO_workingTest(){
        //given
        Region region = new TestRegionBuilder().withName("NEW").build();

        //when
        ResponseRegionDTO responseRegionDTO = regionMapper.toResponseRegionDTO(region);

        //then
        assertEquals(region.getName(),responseRegionDTO.name());
    }

    @Test
    void updateRegionFromDTO_workingTest(){
        //given
        String originalName = "ORIGINAL";
        String updatedName = "UPDATED";

        boolean updatedEnable = false;

        Region region = new TestRegionBuilder().withName(originalName).build();
        region.setEnable(true);

        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().withName(updatedName).withIsEnable(updatedEnable).build();

        //when
        regionMapper.updateRegionFromDTO(updateRegionDTO,region);

        //then
        assertEquals(updatedName,region.getName());
        assertFalse(region.isEnable());
    }
}