package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
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

    @Test
    void updateRegion_entityDoesNotExistThrowsException(){
        //given
        Long randomId = 1234L;
        when(regionRepository.existsById(randomId)).thenReturn(false);

        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().build();

        //when
        assertThrows(EntityNotFoundException.class,() -> regionService.updateRegion(randomId,updateRegionDTO),
                "Region with id " + randomId + " does not exist");

        //then
    }

    @Test
    void updateRegion_idIsNullThrowsException(){
        //given
        Long nullNumber = null;

        UpdateRegionDTO updateRegionDTO = new TestUpdateRegionDTO().build();

        //when
        assertThrows(NullPointerException.class,() -> regionService.updateRegion(nullNumber,updateRegionDTO),
                "Id cannot be null");

        //then
    }

    @Test
    void updateRegion_argumentDTOisNullThrowsException(){
        //given
        Long randomId = 4321L;

        UpdateRegionDTO nullDTO = null;

        //when
        assertThrows(NullPointerException.class,() -> regionService.updateRegion(randomId,nullDTO));
        //then
    }

    @Test
    void findAll_workingTest(){
        //given
        Region region1 = new TestRegionBuilder().withName("FIRST").build();
        Region region2 = new TestRegionBuilder().withName("SECOND").build();
        Region region3 = new TestRegionBuilder().withName("THIRD").build();

        List<Region> regions = List.of(region1,region2,region3);
        when(regionRepository.findAll()).thenReturn(regions);

        ResponseRegionDTO responseRegionDTO1 = new TestResponseRegionDTO().withName(region1.getName()).build();
        ResponseRegionDTO responseRegionDTO2 = new TestResponseRegionDTO().withName(region2.getName()).build();
        ResponseRegionDTO responseRegionDTO3 = new TestResponseRegionDTO().withName(region3.getName()).build();

        when(regionMapper.toResponseRegionDTO(region1)).thenReturn(responseRegionDTO1);
        when(regionMapper.toResponseRegionDTO(region2)).thenReturn(responseRegionDTO2);
        when(regionMapper.toResponseRegionDTO(region3)).thenReturn(responseRegionDTO3);

        //when
        List<ResponseRegionDTO> serviceResponse = regionService.findAll();

        //then
        assertTrue(serviceResponse.contains(responseRegionDTO1));
        assertTrue(serviceResponse.contains(responseRegionDTO2));
        assertTrue(serviceResponse.contains(responseRegionDTO3));
        assertEquals(3,serviceResponse.size());
    }

    @Test
    void findAll_emptyListWorkingTest(){
        //given
        when(regionRepository.findAll()).thenReturn(new ArrayList<>());

        //when
        List<ResponseRegionDTO> serviceResponse = regionService.findAll();

        //then
        assertEquals(0,serviceResponse.size());
    }

    @Test
    void findById_workingTest(){
        //given
        String first = "FIRST";
        String second = "SECOND";
        Long region1Id = 1L;

        Region region1 = new TestRegionBuilder().withName(first).build();

        when(regionRepository.findById(region1Id)).thenReturn(Optional.of(region1));
        ResponseRegionDTO responseRegionDTO1 = new TestResponseRegionDTO().withName(region1.getName()).withId(region1Id).build();

        when(regionMapper.toResponseRegionDTO(region1)).thenReturn(responseRegionDTO1);

        //when
        ResponseRegionDTO serviceResponse = regionService.findById(region1Id);

        //then
        assertEquals(first,serviceResponse.name());
        assertEquals(region1Id,serviceResponse.id());

        assertNotEquals(second,serviceResponse.name());

        verify(regionRepository,times(1)).findById(any(Long.class));
        verify(regionMapper,times(1)).toResponseRegionDTO(any(Region.class));
    }

    @Test
    void findById_entityDoesNotExistThrowsException(){
        //given
        Long notExistingEntityId = 1231232L;

        //when
        assertThrows(EntityNotFoundException.class, () -> regionService.findById(notExistingEntityId),
                "Cannot find region by id " + notExistingEntityId);

        //then
        verify(regionMapper,never()).toResponseRegionDTO(any(Region.class));
    }

    @Test
    void findById_idIsNullThrowsException(){
        //given
        Long nullId = null;

        //when
        assertThrows(NullPointerException.class,() -> regionService.findById(nullId),
                "Id cannot be null");

        //then
        verify(regionRepository,never()).findById(any(Long.class));
        verify(regionMapper,never()).toResponseRegionDTO(any(Region.class));
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1234L;

        when(regionRepository.existsById(id)).thenReturn(true);

        //when
        boolean doExist = regionService.exists(id);

        //then
        assertTrue(doExist);
        verify(regionRepository,times(1)).existsById(id);
    }

    @Test
    void existsById_idIsNullThrowsException(){
        //given
        Long nullId = null;

        //when
        assertThrows(NullPointerException.class, () -> regionService.findById(nullId),
        "Id cannot be null");

        //then
        verify(regionRepository,never()).findById(any(Long.class));
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "NAME";
        when(regionRepository.existsByName(name)).thenReturn(true);

        //when
        boolean doExist = regionService.exists(name);

        //then
        assertTrue(doExist);
    }

    @Test
    void existsByName_nameIsNull(){
        //given
        String nullName = null;

        //when
        assertThrows(NullPointerException.class, () -> regionService.exists(nullName),
                "Name cannot be null");

        //then
        verify(regionRepository,never()).existsByName(any(String.class));
    }

    @Test
    void deleteRegion_workingTest(){
        //given
        String name = "NAME";
        Long id = 1L;

        when(regionRepository.existsById(id)).thenReturn(true);

        //when
        regionService.deleteRegion(id);

        //then
        verify(regionRepository,times(1)).deleteById(id);
    }

    @Test
    void deleteRegion_idIsNullThrowsException(){
        //given
        Long nullId = null;

        //when
        assertThrows(NullPointerException.class,() -> regionService.deleteRegion(nullId),
                "Id cannot be null");

        //then
        verify(regionRepository,never()).existsById(any(Long.class));
        verify(regionRepository,never()).deleteById(any(Long.class));
    }

    @Test
    void deleteRegion_entityDoesNotExistThrowsException(){
        //given
        Long randomId = 2154L;

        //when
        assertThrows(EntityNotFoundException.class,() -> regionService.deleteRegion(randomId),
                "Cannot find region by id" + randomId);

        //then
        verify(regionRepository,never()).deleteById(any(Long.class));
    }




}