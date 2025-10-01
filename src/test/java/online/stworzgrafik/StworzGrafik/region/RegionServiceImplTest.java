package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestCreateRegionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestUpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {
    @InjectMocks
    private RegionServiceImpl regionService;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionBuilder regionBuilder;

    @Mock
    private RegionMapperImpl regionMapper;

    @Test
    void createRegion_workingTest(){
        //given
        String name = "BESTREGION";

        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName(name).build();
        when(regionRepository.existsByName(name)).thenReturn(false);

        Region region = new TestRegionBuilder().withName(name).build();
        when(regionBuilder.createRegion(createRegionDTO.name())).thenReturn(region);

        when(regionRepository.save(region)).thenReturn(region);

        ResponseRegionDTO responseRegionDTO = new TestResponseRegionDTO().withName(name).build();
        when(regionMapper.toResponseRegionDTO(region)).thenReturn(responseRegionDTO);

        //when
        ResponseRegionDTO serviceResponse = regionService.createRegion(createRegionDTO);

        //then
        assertEquals(name,serviceResponse.name());

        verify(regionRepository,times(1)).save(region);
        verify(regionRepository,times(1)).existsByName(anyString());
        verify(regionMapper,times(1)).toResponseRegionDTO(any(Region.class));
    }

    @Test
    void createRegion_entityAlreadyExistThrowsException(){
        //given
        String name = "NEWNAME";

        CreateRegionDTO createRegionDTO = new TestCreateRegionDTO().withName(name).build();
        when(regionRepository.existsByName(name)).thenReturn(true);

        //when
        assertThrows(EntityExistsException.class, () -> regionService.createRegion(createRegionDTO),
                "Region with name " + name + "already exist");

        //then
    }

    @Test
    void createRegion_argumentDTOisNullThrowsException(){
        //given
        CreateRegionDTO createRegionDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> regionService.createRegion(createRegionDTO));
        //then
    }

    @Test
    void updateRegion_workingTest(){
        //given
        String originalName = "ORIGINALNAME";
        long id = 1234L;

        Region region = new TestRegionBuilder().withName(originalName).build();
        when(regionRepository.existsById(id)).thenReturn(true);
        when(regionRepository.findById(id)).thenReturn(Optional.ofNullable(region));

        String updatedName = "UPDATEDNAME";
        boolean enable = false;

        Region savedRegion = new TestRegionBuilder().withName(updatedName).build();
        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().withName(updatedName).withIsEnable(enable).build();

        when(regionRepository.save(any(Region.class))).thenReturn(savedRegion);

        ResponseRegionDTO responseRegionDTO = new TestResponseRegionDTO().withIsEnable(enable).withName(updatedName).withId(id).build();
        when(regionMapper.toResponseRegionDTO(any(Region.class))).thenReturn(responseRegionDTO);

        //when
        ResponseRegionDTO serviceResponse = regionService.updateRegion(id, updateRegionDTO);

        //then
        assertEquals(updatedName,serviceResponse.name());
        assertEquals(id,serviceResponse.id());
        assertEquals(enable,serviceResponse.enable());

        verify(regionRepository,times(1)).existsById(id);
        verify(regionRepository,times(1)).findById(id);
        verify(regionRepository,times(1)).save(any(Region.class));
    }

    //time to build rest of serviceImpl test, now go for updateRegion_ GL! Ave Maryja!

}