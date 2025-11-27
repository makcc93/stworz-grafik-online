package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceImplTest {
    @Mock
    ShiftMapper shiftMapper;

    @Mock
    ShiftRepository shiftRepository;

    @Mock
    ShiftBuilder shiftBuilderMock;

    @InjectMocks
    ShiftServiceImpl shiftServiceImpl;

    @Test
    void save_WorkingTest(){
        //given
        LocalTime startHour = LocalTime.of(14,0);
        LocalTime endHour = LocalTime.of(20,0);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        when(shiftRepository.save(shift)).thenReturn(shift);

        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withId(shift.getId()).withStartHour(startHour).withEndHour(endHour).build();
        when(shiftMapper.toShiftDto(shift)).thenReturn(responseShiftDTO);

        //when
        ResponseShiftDTO serviceResponse = shiftServiceImpl.save(shift);

        //then
        assertEquals(startHour,serviceResponse.startHour());
        assertEquals(endHour, serviceResponse.endHour());
        assertEquals(shift.getStartHour(),serviceResponse.startHour());
        assertEquals(shift.getEndHour(),serviceResponse.endHour());

        verify(shiftRepository,times(1)).save(shift);
    }

    @Test
    void save_theOrderOfProcessInvocation(){
        //given
        LocalTime startHour = LocalTime.of(14,0);
        LocalTime endHour = LocalTime.of(20,0);
        when(shiftRepository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(false);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        when(shiftRepository.save(shift)).thenReturn(shift);

        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withId(shift.getId()).withStartHour(startHour).withEndHour(endHour).build();
        when(shiftMapper.toShiftDto(shift)).thenReturn(responseShiftDTO);

        //when
        shiftServiceImpl.save(shift);

        //then
        verify(shiftRepository,times(1)).existsByStartHourAndEndHour(startHour,endHour);
        verify(shiftRepository,times(1)).save(shift);
        verify(shiftMapper).toShiftDto(shift);
    }

    @Test
    void save_ShiftIsNull(){
        //given
        Shift shift = null;
        //when
        assertThrows(NullPointerException.class,() -> shiftServiceImpl.save(shift));

        //then
        verify(shiftRepository,never()).save(any());
    }

    @Test
    void saveEntity_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(2,0);
        LocalTime endHour = LocalTime.of(20,0);
        int hoursDifference = endHour.getHour() - startHour.getHour();

        when(shiftRepository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(false);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

        when(shiftRepository.save(shift)).thenReturn(shift);

        //when
        Shift saved = shiftServiceImpl.saveEntity(shift);

        //then
        assertEquals(saved.getStartHour(), shift.getStartHour());
        assertEquals(saved.getEndHour(), shift.getEndHour());

        assertEquals(startHour.getHour(), saved.getStartHour().getHour());
        assertEquals(endHour.getHour(), saved.getEndHour().getHour());
    }

    @Test
    void saveEntity_entityIsNull(){
        //given
        Shift shift = null;

        //when
        assertThrows(NullPointerException.class,() -> shiftServiceImpl.saveEntity(shift));


        //then
        verify(shiftRepository,never()).save(any(Shift.class));
    }

    @Test
    void create_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(9, 0);
        LocalTime endHour = LocalTime.of(20, 0);

        when(shiftRepository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(false);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();
        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withStartHour(startHour).withEndHour(endHour).build();
        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

        when(shiftRepository.save(shift)).thenReturn(shift);
        when(shiftMapper.toShiftDto(any(Shift.class))).thenReturn(responseShiftDTO);
        when(shiftBuilderMock.createShift(any(),any())).thenReturn(shift);

        //when
        ResponseShiftDTO serviceResponse = shiftServiceImpl.create(shiftHoursDTO);

        // then
        assertEquals(9,serviceResponse.startHour().getHour());
        assertEquals(11,serviceResponse.length());
    }

    @Test
    void create_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(20, 0);
        LocalTime endHour = LocalTime.of(8,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        assertThrows(IllegalArgumentException.class,() -> shiftServiceImpl.create(shiftHoursDTO));

        //then
        verify(shiftRepository,never()).save(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1L;

        when(shiftRepository.existsById(id)).thenReturn(true);

        //when
        boolean exists = shiftServiceImpl.exists(id);


        //then
        assertTrue(exists);
        verify(shiftRepository,times(1)).existsById(id);
    }

    @Test
    void existsById_shouldReturnFalse(){
        //given
        Long id = 1L;

        when(shiftRepository.existsById(id)).thenReturn(false);

        //when
        boolean exists = shiftServiceImpl.exists(id);

        //then
        assertFalse(exists);
        verify(shiftRepository,times(1)).existsById(id);
    }

    @Test
    void existsByLocalTime_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(15,0);

        when(shiftRepository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(true);

        //when
        boolean exists = shiftServiceImpl.exists(startHour, endHour);

        //then
        assertTrue(exists);
        verify(shiftRepository,times(1)).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void existsByLocalTime_endHourIsBeforeStartHourException(){
        //given
        LocalTime endHour = LocalTime.of(10,0);
        LocalTime startHour = LocalTime.of(15,0);

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.exists(startHour, endHour));


        //then
        assertEquals("End hour cannot be before start hour",exception.getMessage());
        verify(shiftRepository,never()).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void delete_workingTest() {
        //given
        Long id = 1L;
        when(shiftRepository.existsById(id)).thenReturn(true);

        //when
        shiftServiceImpl.delete(id);

        //then
        verify(shiftRepository, times(1)).deleteById(id);
    }

    @Test
    void delete_shiftDoesNotExistsException(){
        //given
        Long id = 2L;
        when(shiftRepository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftServiceImpl.delete(id));

        //then
        assertEquals("Shift with id " + id + " does not exist",exception.getMessage());
        verify(shiftRepository,never()).deleteById(id);
    }

    @Test
    void findAll_workingTest(){
        //given
        Shift shift1 = new TestShiftBuilder().withStartHour(LocalTime.of(10, 0)).withEndHour(LocalTime.of(20, 0)).build();
        Shift shift2 = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(19, 0)).build();
        Shift shift3 = new TestShiftBuilder().withStartHour(LocalTime.of(9, 0)).withEndHour(LocalTime.of(15, 0)).build();

        when(shiftRepository.findAll()).thenReturn(List.of(shift1,shift2,shift3));

        //when
        List<ResponseShiftDTO> shiftList = shiftServiceImpl.findAll();

        //then
        assertEquals(3,shiftList.size());

        verify(shiftRepository,times(1)).findAll();
    }

    @Test
    void findById_workingTest(){
        //given
        Long id = 1L;
        LocalTime startHour = LocalTime.of(9, 0);
        LocalTime endHour = LocalTime.of(20, 0);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withStartHour(startHour).withEndHour(endHour).build();

        when(shiftMapper.toShiftDto(shift)).thenReturn(responseShiftDTO);
        when(shiftRepository.findById(id)).thenReturn(Optional.ofNullable(shift));

        //when
        ResponseShiftDTO responseDto = shiftServiceImpl.findById(id);

        //then
        assertEquals(startHour.getHour(),responseDto.startHour().getHour());
        assertEquals(endHour.getHour(),responseDto.endHour().getHour());

        verify(shiftRepository,times(1)).findById(id);
        verify(shiftMapper).toShiftDto(any(Shift.class));
    }

    @Test
    void findById_shiftWithIdDoesNotExistException(){
        //given
        Long id = 1L;
        when(shiftRepository.findById(id)).thenThrow(new IllegalArgumentException());

        //when
        assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.findById(id));

        //then
        verify(shiftMapper,never()).toShiftDto(any(Shift.class));
    }

    @Test
    void getEntityById_workingTest(){
        //given
        Long id = 123L;
        Shift shift = new TestShiftBuilder().build();
        LocalTime startHour = shift.getStartHour();
        LocalTime endHour = shift.getEndHour();

        when(shiftRepository.findById(id)).thenReturn(Optional.ofNullable(shift));

        //when
        Shift serviceResponse = shiftServiceImpl.getEntityById(id);

        //then
        assertEquals(startHour, serviceResponse.getStartHour());
        assertEquals(endHour, serviceResponse.getEndHour());
        verify(shiftRepository,times(1)).findById(id);
    }

    @Test
    void getEntityById_cannotFindEntityException(){
        //given
        Long id = 1L;

        when(shiftRepository.findById(id))
                .thenThrow(new EntityNotFoundException("Cannot find shift by id: " + id));

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> shiftServiceImpl.getEntityById(id));

        //then
        assertEquals("Cannot find shift by id: " + id, exception.getMessage());
        verify(shiftRepository,times(1)).findById(id);
    }

    @Test
    void getLength_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(21,0);
        int length = 11;

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        Integer serviceResponse = shiftServiceImpl.getLength(shiftHoursDTO);

        //then
        assertEquals(length,serviceResponse);
    }

    @Test
    void getLength_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(8,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getLength(shiftHoursDTO));

        //then
        assertEquals("End hour cannot be before start hour", exception.getMessage());
    }

    @Test
    void getLength_startHourEqualsEndHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(15,0);
        LocalTime endHour = LocalTime.of(15,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getLength(shiftHoursDTO));

        //then
        assertEquals("End hour cannot equals start hour", exception.getMessage());
    }

    @Test
    void getDurationHours_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(21,0);
        BigDecimal length = BigDecimal.valueOf(11L);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        BigDecimal serviceResponse = shiftServiceImpl.getDurationHours(shiftHoursDTO);

        //then
        assertEquals(length,serviceResponse);
    }

    @Test
    void getDurationHours_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(8,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getDurationHours(shiftHoursDTO));

        //then
        assertEquals("End hour cannot be before start hour", exception.getMessage());
    }

    @Test
    void getDurationHours_startHourEqualsEndHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(15,0);
        LocalTime endHour = LocalTime.of(15,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getDurationHours(shiftHoursDTO));

        //then
        assertEquals("End hour cannot equals start hour", exception.getMessage());
    }

    @Test
    void getShiftAsArray_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(15, 0);
        int[] expectedArray = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0};

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        int[] serviceResponse = shiftServiceImpl.getShiftAsArray(shiftHoursDTO);

        //then
        assertArrayEquals(expectedArray,serviceResponse);
    }

    @Test
    void getShiftAsArray_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(8,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getShiftAsArray(shiftHoursDTO));

        //then
        assertEquals("End hour cannot be before start hour", exception.getMessage());
    }

    @Test
    void getShiftAsArray_startHourEqualsEndHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(15,0);
        LocalTime endHour = LocalTime.of(15,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getShiftAsArray(shiftHoursDTO));

        //then
        assertEquals("End hour cannot equals start hour", exception.getMessage());
    }
}