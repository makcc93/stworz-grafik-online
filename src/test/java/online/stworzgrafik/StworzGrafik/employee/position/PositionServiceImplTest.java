package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestUpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestCreateRegionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceImplTest {
    @InjectMocks
    private PositionServiceImpl positionService;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PositionMapper positionMapper;

    @Mock
    private PositionBuilder positionBuilder;

    @Test
    void findAll_workingTest(){
        //given
        Position position1 = new TestPositionBuilder().withName("FIRST").build();
        Position position2 = new TestPositionBuilder().withName("SECOND").build();
        Position position3 = new TestPositionBuilder().withName("THIRD").build();
        List<Position> positions = List.of(position1,position2,position3);

        when(positionRepository.findAll()).thenReturn(positions);

        ResponsePositionDTO responsePositionDTO1 = new TestResponsePositionDTO().withName(position1.getName()).build();
        ResponsePositionDTO responsePositionDTO2 = new TestResponsePositionDTO().withName(position2.getName()).build();
        ResponsePositionDTO responsePositionDTO3 = new TestResponsePositionDTO().withName(position3.getName()).build();
        List<ResponsePositionDTO> responseDTOS = List.of(responsePositionDTO1,responsePositionDTO2,responsePositionDTO3);

        when(positionMapper.toResponsePositionDTO(position1)).thenReturn(responsePositionDTO1);
        when(positionMapper.toResponsePositionDTO(position2)).thenReturn(responsePositionDTO2);
        when(positionMapper.toResponsePositionDTO(position3)).thenReturn(responsePositionDTO3);

        //when
        List<ResponsePositionDTO> serviceResponse = positionService.findAll();

        //then
        assertTrue(serviceResponse.contains(responsePositionDTO1));
        assertTrue(serviceResponse.contains(responsePositionDTO2));
        assertTrue(serviceResponse.contains(responsePositionDTO3));
        assertEquals(3,serviceResponse.size());
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponsePositionDTO> serviceResponse = positionService.findAll();

        //then
        assertEquals(0,serviceResponse.size());
        verify(positionRepository).findAll();
    }

    @Test
    void findById_workingTest(){
        //given
        Long id = 1L;
        Position position = new TestPositionBuilder().withName("POSITION").build();

        when(positionRepository.findById(id)).thenReturn(Optional.ofNullable(position));

        ResponsePositionDTO responsePositionDTO = new TestResponsePositionDTO().withId(id).withName(position.getName()).build();

        when(positionMapper.toResponsePositionDTO(position)).thenReturn(responsePositionDTO);

        //when
        ResponsePositionDTO serviceResponse = positionService.findById(id);

        //then
        assertEquals(id,serviceResponse.id());
        assertEquals(position.getName(),serviceResponse.name());

        verify(positionRepository,times(1)).findById(any(Long.class));
    }

    @Test
    void findById_entityNotExistingThrowsException(){
        //given
        Long randomId = 123123L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> positionService.findById(randomId));

        //then
        assertEquals("Cannot find position by id " + randomId, exception.getMessage());
        verify(positionRepository,times(1)).findById(randomId);
    }

    @Test
    void findById_idIsNullThrowsException(){
        //given
        Long nullId = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> positionService.findById(nullId));

        //then
        assertEquals("Id cannot be null", exception.getMessage());
        verify(positionRepository, never()).findById(any());
    }

    @Test
    void createPosition_workingTest(){
        //given
        String name = "NEW POSITION";
        String description = "This is new position";

        CreatePositionDTO createRegionDTO = new TestCreatePositionDTO().withName(name).withDescription(description).build();
        when(positionRepository.existsByName(name)).thenReturn(false);

        Position position = new TestPositionBuilder().withName(name).withDescription(description).build();
        when(positionBuilder.createPosition(any(),any())).thenReturn(position);

        ResponsePositionDTO responsePositionDTO = new TestResponsePositionDTO().withName(name).withDescription(description).build();
        when(positionMapper.toResponsePositionDTO(position)).thenReturn(responsePositionDTO);

        when(positionRepository.save(position)).thenReturn(position);

        //when
        ResponsePositionDTO serviceResponse = positionService.createPosition(createRegionDTO);

        //then
        assertEquals(name,serviceResponse.name());
        assertEquals(description,serviceResponse.description());

        verify(positionRepository,times(1)).existsByName(any());
        verify(positionBuilder,times(1)).createPosition(any(),any());
        verify(positionMapper,times(1)).toResponsePositionDTO(any());
    }

    @Test
    void createPosition_entityWithThisNameAlreadyExistsThrowsException(){
        //given
        String name = "ALREADY EXISTS";
        when(positionRepository.existsByName(name)).thenReturn(true);

        CreatePositionDTO createRegionDTO = new TestCreatePositionDTO().withName(name).build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> positionService.createPosition(createRegionDTO));

        //then
        assertEquals("Position with name " + name + " already exists", exception.getMessage());

        verify(positionRepository, times(1)).existsByName(name);
        verify(positionBuilder, never()).createPosition(any(),any());
        verify(positionMapper,never()).toResponsePositionDTO(any());
    }

    @Test
    void createPosition_dtoIsNullThrowsException(){
        //given
        CreatePositionDTO createPositionDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> positionService.createPosition(createPositionDTO));

        //then
        verify(positionRepository,never()).existsByName(any());
        verify(positionBuilder,never()).createPosition(any(),any());
        verify(positionMapper,never()).toResponsePositionDTO(any());
    }

    @Test
    void updatePosition_workingTest(){
        //given
        Long id = 1L;
        String originalName = "ORIGINAL NAME";
        String originalDescription = "ORIGINAL DESCRIPTION";
        Position position = new TestPositionBuilder().withName(originalName).withDescription(originalDescription).build();

        String newName = "UPDATE NAME";
        String newDescription = "UPDATE DESCRIPTION";
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().withName(newName).withDescription(newDescription).build();

        when(positionRepository.findById(id)).thenReturn(Optional.ofNullable(position));

        ResponsePositionDTO responsePositionDTO =
                new TestResponsePositionDTO().withName(updatePositionDTO.name()).withDescription(updatePositionDTO.description()).withId(id).build();

        when(positionMapper.toResponsePositionDTO(position)).thenReturn(responsePositionDTO);

        //when
        ResponsePositionDTO updated = positionService.updatePosition(id, updatePositionDTO);

        //then
        assertEquals(newName,updated.name());
        assertEquals(newDescription,updated.description());
        assertEquals(id, updated.id());
    }

    @Test
    void updatePosition_entityDoesNotExistThrowsException(){
        //given
        Long id = 0L;
        when(positionRepository.findById(id)).thenThrow(EntityNotFoundException.class);

        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> positionService.updatePosition(id, updatePositionDTO));

        //then
        verify(positionRepository,times(1)).findById(id);
        verify(positionMapper, never()).updatePosition(any(),any());
        verify(positionMapper,never()).toResponsePositionDTO(any());
    }

    @Test
    void updatePosition_dtoIsNullThrowsException(){
        //given
        Long id = 1L;
        UpdatePositionDTO updatePositionDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> positionService.updatePosition(id, updatePositionDTO));

        //then
        verify(positionRepository,never()).findById(id);
        verify(positionMapper, never()).updatePosition(any(),any());
        verify(positionMapper,never()).toResponsePositionDTO(any());
    }

    @Test
    void updatePosition_idIsNullThrowsException(){
        //given
        Long id = null;
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().build();

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> positionService.updatePosition(id, updatePositionDTO));

        //then
        assertEquals("Id cannot be null", exception.getMessage());
    }

    @Test
    void deletePosition_workingTest(){
        //given
        Long id = 1L;

        when(positionRepository.existsById(id)).thenReturn(true);

        //when
        positionService.deletePosition(id);

        //then
        verify(positionRepository, times(1)).deleteById(id);
    }

    @Test
    void deletePosition_entityDoesNotExistThrowsException(){
        //given
        Long id = 1L;

        when(positionRepository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> positionService.deletePosition(id));

        //then
        assertEquals("Position with id " + id + " does not exist", exception.getMessage());

        verify(positionRepository,never()).deleteById(any());
    }

    @Test
    void deletePosition_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> positionService.deletePosition(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(positionRepository, never()).existsById(any());
        verify(positionRepository, never()).deleteById(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1L;
        when(positionRepository.existsById(id)).thenReturn(true);

        //when
        boolean response = positionService.exists(id);

        //then
        assertTrue(response);
    }

    @Test
    void existsById_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> positionService.exists(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());
        verify(positionRepository,never()).existsById(any());
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "NAME";
        when(positionRepository.existsByName(name)).thenReturn(true);

        //when
        boolean response = positionService.exists(name);

        //then
        assertTrue(response);
    }

    @Test
    void existsByName_nameIsNullThrowsException(){
        //given
        String name = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> positionService.exists(name));

        //then
        assertEquals("Name cannot be null", exception.getMessage());
        verify(positionRepository,never()).existsById(any());
    }
}