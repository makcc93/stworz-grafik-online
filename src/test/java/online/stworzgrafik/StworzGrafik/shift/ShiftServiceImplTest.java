package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift.TestResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift.TestShiftHoursDTO;
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
        //given
        LocalTime startHour = LocalTime.of(14,0);
        LocalTime endHour = LocalTime.of(20,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();
        Shift entity = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withId(entity.id).withStartHour(startHour).withEndHour(endHour).build();

        when(shiftMapper.toEntity(shiftHoursDTO)).thenReturn(entity);
        when(shiftMapper.toShiftDto(entity)).thenReturn(responseShiftDTO);
        when(repository.save(entity)).thenReturn(entity);

        //when
        ResponseShiftDTO saved = service.saveDto(shiftHoursDTO);

        //then
        assertEquals(shiftHoursDTO.startHour(),saved.startHour());
        assertEquals(shiftHoursDTO.endHour(), saved.endHour());
        assertEquals(entity.startHour,saved.startHour());
        assertEquals(entity.endHour,saved.endHour());

        verify(repository,times(1)).save(entity);
    }

    @Test
    void saveDto_theOrderOfProcessInvocation(){
        //given
        LocalTime startHour = LocalTime.of(14,0);
        LocalTime endHour = LocalTime.of(20,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();
        Shift entity = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withId(entity.id).withStartHour(startHour).withEndHour(endHour).build();


        when(shiftMapper.toEntity(shiftHoursDTO)).thenReturn(entity);
        when(shiftMapper.toShiftDto(entity)).thenReturn(responseShiftDTO);
        when(repository.save(entity)).thenReturn(entity);

        //when
        service.saveDto(shiftHoursDTO);

        //then
        verify(shiftMapper).toEntity(shiftHoursDTO);
        verify(shiftMapper).toShiftDto(entity);
    }

    @Test
    void saveDto_ShiftIsNull(){
        //given

        //when
        assertThrows(NullPointerException.class,() -> service.saveDto(null));

        //then
        verify(repository,never()).save(any());
    }

    @Test
    void saveEntity_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(2,0);
        LocalTime endHour = LocalTime.of(20,0);
        int hoursDifference = endHour.getHour() - startHour.getHour();

        when(repository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(false);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

        when(repository.save(shift)).thenReturn(shift);

        //when
        Shift saved = service.saveEntity(shift);

        //then
        assertEquals(saved.startHour,shift.startHour);
        assertEquals(saved.endHour,shift.endHour);
        assertEquals(saved.getLength(),shift.getLength());

        assertEquals(startHour.getHour(),saved.startHour.getHour());
        assertEquals(endHour.getHour(),saved.endHour.getHour());
        assertEquals(hoursDifference,saved.getLength());
    }

    @Test
    void saveEntity_entityIsNull(){
        //given
        Shift shift = null;

        //when
        assertThrows(NullPointerException.class,() -> service.saveEntity(shift));


        //then
        verify(repository,never()).save(any(Shift.class));
    }

    @Test
    void create_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(9, 0);
        LocalTime endHour = LocalTime.of(20, 0);

        when(repository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(false);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();
        ResponseShiftDTO responseShiftDTO = new TestResponseShiftDTO().withStartHour(startHour).withEndHour(endHour).build();
        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

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
        //given
        ShiftHoursDTO shiftHoursDTO = null;

        //when
        assertThrows(NullPointerException.class,() -> service.create(shiftHoursDTO));

        //then
    }

    @Test
    void create_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(20, 0);
        LocalTime endHour = LocalTime.of(8,0);

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        assertThrows(IllegalArgumentException.class,() -> service.create(shiftHoursDTO));

        //then
        verify(repository,never()).save(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(true);

        //when
        boolean exists = service.exists(id);


        //then
        assertTrue(exists);
        verify(repository,times(1)).existsById(id);
    }

    @Test
    void existsById_shouldReturnFalse(){
        //given
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(false);

        //when
        boolean exists = service.exists(id);

        //then
        assertFalse(exists);
        verify(repository,times(1)).existsById(id);
    }

    @Test
    void existsById_idIsNull(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(id));

        //then
        assertEquals("Id cannot be null",exception.getMessage());
        verify(repository,never()).existsById(any());
    }

    @Test
    void existsByLocalTime_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(15,0);

        when(repository.existsByStartHourAndEndHour(startHour,endHour)).thenReturn(true);

        //when
        boolean exists = service.exists(startHour, endHour);

        //then
        assertTrue(exists);
        verify(repository,times(1)).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void existsByLocalTime_startHourIsNull(){
        //given
        LocalTime startHour = null;
        LocalTime endHour = LocalTime.of(15,0);

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(startHour,endHour));

        //then
        assertEquals("Start hour cannot be null",exception.getMessage());
        verify(repository,never()).existsByStartHourAndEndHour(any(LocalTime.class),any(LocalTime.class));
    }

    @Test
    void existsByLocalTime_endHourIsNull(){
        //given
        LocalTime startHour = LocalTime.of(15,0);
        LocalTime endHour = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(startHour,endHour));

        //then
        assertEquals("End hour cannot be null",exception.getMessage());
        verify(repository,never()).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void existsByLocalTime_endHourIsBeforeStartHourException(){
        //given
        LocalTime endHour = LocalTime.of(10,0);
        LocalTime startHour = LocalTime.of(15,0);

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.exists(startHour, endHour));


        //then
        assertEquals("End hour cannot be before start hour",exception.getMessage());
        verify(repository,never()).existsByStartHourAndEndHour(startHour,endHour);
    }

    @Test
    void delete_workingTest(){
        //given
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);

        //when
        service.delete(id);

        //then
        verify(repository,times(1)).deleteById(id);
    }

    @Test
    void delete_idIsNull(){
        //given
        Long id = null;


        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.delete(id));

        //then
        assertEquals("Id cannot be null",exception.getMessage());
    }

    @Test
    void delete_shiftDoesNotExistsException(){
        //given
        Long id = 2L;
        when(repository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.delete(id));

        //then
        assertEquals("Shift with id " + id + " does not exist",exception.getMessage());
        verify(repository,never()).deleteById(id);
    }

    @Test
    void findAll_workingTest(){
        //given
        Shift shift1 = new TestShiftBuilder().withStartHour(LocalTime.of(10, 0)).withEndHour(LocalTime.of(20, 0)).build();
        Shift shift2 = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(19, 0)).build();
        Shift shift3 = new TestShiftBuilder().withStartHour(LocalTime.of(9, 0)).withEndHour(LocalTime.of(15, 0)).build();

        when(repository.findAll()).thenReturn(List.of(shift1,shift2,shift3));

        //when
        List<ResponseShiftDTO> shiftList = service.findAll();

        //then
        assertEquals(3,shiftList.size());

        verify(repository,times(1)).findAll();
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
        when(repository.findById(id)).thenReturn(Optional.ofNullable(shift));

        //when
        ResponseShiftDTO responseDto = service.findById(id);

        //then
        assertEquals(startHour.getHour(),responseDto.startHour().getHour());
        assertEquals(endHour.getHour(),responseDto.endHour().getHour());
        assertEquals(shift.getLength(),responseDto.length());

        verify(repository,times(1)).findById(id);
        verify(shiftMapper).toShiftDto(any(Shift.class));
    }

    @Test
    void findById_idIsNull(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findById(id));

        //then
        assertEquals("Id cannot be null",exception.getMessage());
        verify(repository,never()).findById(any(Long.class));
    }

    @Test
    void findById_shiftWithIdDoesNotExistException(){
        //given
        Long id = 1L;
        when(repository.findById(id)).thenThrow(new IllegalArgumentException());

        //when
        assertThrows(IllegalArgumentException.class, () -> service.findById(id));

        //then
        verify(shiftMapper,never()).toShiftDto(any(Shift.class));
    }

    @Test
    void findEntityById_workingTest(){
        //given
        Long id = 1L;
        Shift shift = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(20, 0)).build();
        shift.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(shift));

        //when
        Shift entityById = service.findEntityById(id);

        //then
        assertEquals(1, entityById.getId());
        verify(repository,times(1)).findById(id);
    }

    @Test
    void findEntityById_idIsNull(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findEntityById(id));

        //then
        assertEquals("Id cannot be null",exception.getMessage());
        verify(repository,never()).findById(any());
    }

    @Test
    void findEntityById_cannotFindEntityException(){
        //given
        Long id = 1L;
        Shift shift = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(20, 0)).build();
        shift.setId(id);

        when(repository.findById(id)).thenThrow(new IllegalArgumentException());

        //when
        assertThrows(IllegalArgumentException.class,() -> service.findEntityById(id));

        //then
    }
}