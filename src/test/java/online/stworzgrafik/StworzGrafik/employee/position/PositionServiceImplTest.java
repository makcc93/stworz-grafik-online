package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
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
}