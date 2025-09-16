package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    ShiftRepository repository;

    @Mock
    ShiftBuilder shiftBuilderMock;

    @InjectMocks
    ShiftServiceImpl service;

    @Test
    void saveDto_WorkingTest(){
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(14,0),LocalTime.of(20,0));
        Shift entity = new ShiftBuilder().createShift(LocalTime.of(14,0),LocalTime.of(20,0));
        ResponseShiftDTO responseShiftDTO = new ResponseShiftDTO(1L,LocalTime.of(14,0),LocalTime.of(20,0),6);


        when(shiftMapper.toEntity(shiftHoursDTO)).thenReturn(entity);

        when(shiftMapper.toShiftDto(entity)).thenReturn(responseShiftDTO);

        when(repository.save(entity)).thenReturn(entity);

        ResponseShiftDTO saved = service.saveDto(shiftHoursDTO);

        verify(repository,times(1)).save(entity);

        assertEquals(saved.endHour(),shiftHoursDTO.endHour());
        assertEquals(20,saved.endHour().getHour());
        assertEquals(entity.startHour,saved.startHour());
    }

    @Test
    void saveDto_theOrderOfProcessInvocation(){
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(14,0),LocalTime.of(20,0));
        Shift entity = new ShiftBuilder().createShift(LocalTime.of(14,0),LocalTime.of(20,0));
        ResponseShiftDTO responseShiftDTO = new ResponseShiftDTO(1L,LocalTime.of(14,0),LocalTime.of(20,0),6);

        when(shiftMapper.toEntity(shiftHoursDTO)).thenReturn(entity);

        when(shiftMapper.toShiftDto(entity)).thenReturn(responseShiftDTO);

        when(repository.save(entity)).thenReturn(entity);

        service.saveDto(shiftHoursDTO);

        verify(shiftMapper).toEntity(shiftHoursDTO);
        verify(shiftMapper).toShiftDto(entity);
    }

    @Test
    void saveDto_ShiftIsNull(){
        assertThrows(NullPointerException.class,() -> service.saveDto(null));

        verify(repository,never()).save(any());
    }

    @Test
    void saveEntity_workingTest(){
        Shift shift = new ShiftBuilder().createShift(LocalTime.of(2,0),LocalTime.of(20,0));

        when(repository.save(shift)).thenReturn(shift);

        Shift saved = service.saveEntity(shift);

        assertEquals(saved.startHour,shift.startHour);
        assertEquals(saved.endHour,shift.endHour);
        assertEquals(saved.getLength(),shift.getLength());

        assertEquals(2,saved.startHour.getHour());
        assertEquals(20,saved.endHour.getHour());
        assertEquals(18,saved.getLength());
    }

    @Test
    void saveEntity_entityIsNull(){
        Shift shift = null;

        assertThrows(NullPointerException.class,() -> service.saveEntity(shift));

        verify(repository,never()).save(any(Shift.class));
    }

    @Test
    void create_workingTest(){
        //given
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(9, 0), LocalTime.of(20, 0));
        ResponseShiftDTO responseShiftDTO = new ResponseShiftDTO(1L,LocalTime.of(9, 0), LocalTime.of(20, 0),11);
        Shift shift = mock(Shift.class);
        shift.setId(1L);
        shift.setStartHour(LocalTime.of(9,0));
        shift.setEndHour(LocalTime.of(20,0));

        when(repository.save(shift)).thenReturn(shift);
        when(shiftMapper.toShiftDto(any(Shift.class))).thenReturn(responseShiftDTO);
        when(shiftBuilderMock.createShift(any(),any())).thenReturn(shift);

        //when
        ResponseShiftDTO serviceResponse = service.create(shiftHoursDTO);

        // then
        assertEquals(9,serviceResponse.startHour().getHour());
        assertEquals(11,serviceResponse.length());
    }

    @Test
    void create_shiftHoursDtoIsNull(){
        ShiftHoursDTO shiftHoursDTO = null;

        assertThrows(NullPointerException.class,() -> service.create(shiftHoursDTO));
    }

    @Test
    void create_endHourIsBeforeStartHourException(){
        LocalTime startHour = LocalTime.of(20, 0);
        LocalTime endHour = LocalTime.of(8,0);
        assertThrows(IllegalArgumentException.class,() -> new ShiftBuilder().createShift(startHour,endHour));

        verify(repository,never()).save(any());
    }

    @Test
    void existsById_workingTest(){
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(true);

        boolean exists = service.exists(id);

        assertTrue(exists);
        verify(repository,times(1)).existsById(id);
    }

    @Test
    void existsById_shouldReturnFalse(){
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(false);

        boolean exists = service.exists(id);

        assertFalse(exists);
        verify(repository,times(1)).existsById(id);
    }

    @Test
    void existsById_idIsNull(){
        Long id = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(id));

        assertEquals("Id cannot be null",exception.getMessage());
        verify(repository,never()).existsById(id);
    }

    @Test
    void existsByLocalTime_workingTest(){
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(15,0);

        when(repository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(true);

        boolean exists = service.exists(startHour, endHour);

        assertTrue(exists);
        verify(repository,times(1)).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void existsByLocalTime_startHourIsNull(){
        LocalTime startHour = null;
        LocalTime endHour = LocalTime.of(15,0);

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(startHour,endHour));

        assertEquals("Start hour cannot be null",exception.getMessage());
        verify(repository,never()).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void existsByLocalTime_endHourIsNull(){
        LocalTime startHour = LocalTime.of(15,0);
        LocalTime endHour = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(startHour,endHour));

        assertEquals("End hour cannot be null",exception.getMessage());
        verify(repository,never()).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void existsByLocalTime_endHourIsBeforeStartHourException(){
        LocalTime endHour = LocalTime.of(10,0);
        LocalTime startHour = LocalTime.of(15,0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.exists(startHour, endHour));

        assertEquals("End hour cannot be before start hour",exception.getMessage());
        verify(repository,never()).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void delete_workingTest(){
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository,times(1)).deleteById(id);
    }

    @Test
    void delete_idIsNull(){
        Long id = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.delete(id));

        assertEquals("Id cannot be null",exception.getMessage());
    }

    @Test
    void delete_shiftDoesNotExistsException(){
        Long id = 2L;

        when(repository.existsById(id)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.delete(id));

        assertEquals("Shift with id " + id + " does not exist",exception.getMessage());
        verify(repository,never()).deleteById(id);
    }

    @Test
    void findAll_workingTest(){
        Shift shift1 = new ShiftBuilder().createShift(LocalTime.of(10, 0), LocalTime.of(20, 0));
        Shift shift2 = new ShiftBuilder().createShift(LocalTime.of(8, 0), LocalTime.of(19, 0));
        Shift shift3 = new ShiftBuilder().createShift(LocalTime.of(9, 0), LocalTime.of(15, 0));
        Shift shift4 = new ShiftBuilder().createShift(LocalTime.of(10, 0), LocalTime.of(15, 0));

        when(repository.findAll()).thenReturn(List.of(shift1,shift2,shift3));

        List<ResponseShiftDTO> shiftList = service.findAll();

        assertEquals(3,shiftList.size());

        verify(repository,times(1)).findAll();
    }

    @Test
    void findById_workingTest(){
        Long id = 1L;
        Shift shift = new ShiftBuilder().createShift(LocalTime.of(9,0),LocalTime.of(20,0));
        ResponseShiftDTO responseShiftDTO = new ResponseShiftDTO(1L, LocalTime.of(9, 0), LocalTime.of(20, 0), 11);

        when(shiftMapper.toShiftDto(shift)).thenReturn(responseShiftDTO);

        when(repository.findById(id)).thenReturn(Optional.ofNullable(shift));

        ResponseShiftDTO responseDto = service.findById(id);

        assertEquals(9,responseDto.startHour().getHour());
        assertEquals(20,responseDto.endHour().getHour());
        assertEquals(shift.getLength(),responseDto.length());

        verify(repository,times(1)).findById(id);
        verify(shiftMapper).toShiftDto(any(Shift.class));
    }

    @Test
    void findById_idIsNull(){
        Long id = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findById(id));

        assertEquals("Id cannot be null",exception.getMessage());
        verify(repository,never()).findById(id);
    }

    @Test
    void findById_shiftWithIdDoesNotExistException(){
        Long id = 1L;

        when(repository.findById(id)).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> service.findById(id));

        verify(shiftMapper,never()).toShiftDto(any(Shift.class));
    }

    @Test
    void findEntityById_workingTest(){
        Long id = 1L;
        Shift shift = new ShiftBuilder().createShift(LocalTime.of(8,0),LocalTime.of(20,0));
        shift.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(shift));

        Shift entityById = service.findEntityById(id);

        assertEquals(1, entityById.getId());

        verify(repository,times(1)).findById(id);
    }

    @Test
    void findEntityById_idIsNull(){
        Long id = null;

        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findEntityById(id));

        assertEquals("Id cannot be null",exception.getMessage());

        verify(repository,never()).findById(any());
    }

    @Test
    void findEntityById_cannotFindEntityException(){
        Long id = 1L;
        Shift shift = new ShiftBuilder().createShift(LocalTime.of(8,0),LocalTime.of(20,0));
        shift.setId(id);

        when(repository.findById(id)).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class,() -> service.findEntityById(id));
    }
}