package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShiftServiceImplIT {

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    ShiftServiceImpl shiftServiceImpl;

    @Test
    void save_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(8, 0);
        LocalTime endHour = LocalTime.of(20, 0);
        int length = endHour.getHour() - startHour.getHour();
        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        //when
        ResponseShiftDTO responseShiftDTO = shiftServiceImpl.save(shift);

        //then
        assertEquals(startHour.getHour(),responseShiftDTO.startHour().getHour());
        assertEquals(endHour.getHour(),responseShiftDTO.endHour().getHour());
        assertEquals(length,responseShiftDTO.length());
        assertTrue(shiftRepository.existsById(responseShiftDTO.id()));
    }

    @Test
    void save_wrongHoursThrowException(){
        //given
        LocalTime startHour = LocalTime.of(23, 0);
        LocalTime endHour = LocalTime.of(6, 0);
        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.save(shift));

        //then
        assertEquals("End hour cannot be before start hour",exception.getMessage());
    }

    @Test
    void saveEntity_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(8,0);
        LocalTime endHour = LocalTime.of(15,0);
        int hoursDifference = endHour.getHour() - startHour.getHour();

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

        //when
        Shift savedEntity = shiftServiceImpl.saveEntity(shift);

        //then
        assertEquals(startHour.getHour(), savedEntity.getStartHour().getHour());
        assertEquals(endHour.getHour(), savedEntity.getEndHour().getHour());

        assertTrue(shiftRepository.existsById(savedEntity.getId()));
    }

    @Test
    void create_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(15, 0);
        LocalTime endHour = LocalTime.of(20, 0);
        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        ResponseShiftDTO responseShiftDTO = shiftServiceImpl.create(shiftHoursDTO);

        //then
        assertEquals(15,responseShiftDTO.startHour().getHour());
        assertEquals(20,responseShiftDTO.endHour().getHour());
        assertEquals(5,responseShiftDTO.length());

        assertNotNull(responseShiftDTO.id());
        assertTrue(responseShiftDTO.id() > 0);

        assertTrue(shiftRepository.existsById(responseShiftDTO.id()));
    }

    @Test
    void create_argumentIsNullThrowException(){
        //given
        ShiftHoursDTO shiftHoursDTO = null;

        //when
        assertThrows(ConstraintViolationException.class,() -> shiftServiceImpl.create(shiftHoursDTO));

        //then
    }

    @Test
    void findEntityById_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(14,0);
        int hoursDifference = endHour.getHour() - startHour.getHour();

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();
        shiftRepository.save(shift);

        //when
        ResponseShiftDTO serviceResponse = shiftServiceImpl.findById(shift.getId());

        //then
        assertTrue(shiftRepository.existsById(serviceResponse.id()));
        assertEquals(startHour.getHour(),serviceResponse.startHour().getHour());
        assertEquals(endHour.getHour(),serviceResponse.endHour().getHour());
        assertEquals(hoursDifference,serviceResponse.length());
    }

    @Test
    void findEntityById_notExistingEntityTest(){
        //given
        Long id = 1234L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftServiceImpl.findById(id));

        //then
        assertEquals("Cannot find shift by id: " + id,exception.getMessage());
    }

    @Test
    void delete_workingTest(){
        //given
        Shift shiftToDelete = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build();
        Shift shiftToLeave = new TestShiftBuilder().withStartHour(LocalTime.of(15, 0)).withEndHour(LocalTime.of(20, 0)).build();
        shiftRepository.save(shiftToDelete);
        shiftRepository.save(shiftToLeave);

        //when
        shiftServiceImpl.delete(shiftToDelete.getId());

        //then
        assertFalse(shiftRepository.existsById(shiftToDelete.getId()));
        assertTrue(shiftRepository.existsById(shiftToLeave.getId()));
    }

    @Test
    void delete_entityNotExistException(){
        //given
        Long id = 999L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftServiceImpl.delete(id));

        //then
        assertEquals("Shift with id " + id + " does not exist", exception.getMessage());
    }

    @Test
    void findAll_workingTest(){
        //given
        Shift shift1 = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build();
        Shift shift2 = new TestShiftBuilder().withStartHour(LocalTime.of(15, 0)).withEndHour(LocalTime.of(20, 0)).build();
        Shift shift3 = new TestShiftBuilder().withStartHour(LocalTime.of(9, 0)).withEndHour(LocalTime.of(15, 0)).build();

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
        shiftRepository.save(shift3);

        //when
        List<ResponseShiftDTO> all = shiftServiceImpl.findAll();

        //then
        assertEquals(3, all.size());

        List<String> stringShifts = all.stream()
                .map(shift -> shift.startHour().getHour() + "-" + shift.endHour().getHour())
                .toList();

        assertTrue(stringShifts.containsAll(List.of("8-14","15-20","9-15")));
    }

    @Test
    void findAll_emptyList(){
        //given

        //when
        List<ResponseShiftDTO> all = shiftServiceImpl.findAll();

        //then
        assertEquals(0, all.size());
    }

    @Test
    void findById_workingTest(){
        //given
        Shift shift1 = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build();
        Shift shift2 = new TestShiftBuilder().withStartHour(LocalTime.of(15, 0)).withEndHour(LocalTime.of(20, 0)).build();
        Shift shift3 = new TestShiftBuilder().withStartHour(LocalTime.of(9, 0)).withEndHour(LocalTime.of(15, 0)).build();

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
        shiftRepository.save(shift3);

        //when
        ResponseShiftDTO responseShiftDTO = shiftServiceImpl.findById(shift2.getId());

        //then
        assertEquals(shift2.getId(),responseShiftDTO.id());
        assertTrue(shiftRepository.existsById(shift2.getId()));
    }

    @Test
    void findById_emptyListThrowException(){
        //given
        Long id = 52L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftServiceImpl.findById(id));

        //then
        assertEquals("Cannot find shift by id: " + id,exception.getMessage());
        assertFalse(shiftRepository.existsById(id));
    }

    @Test
    void existsByHours_workingTest(){
        //given
        LocalTime startHourToTest = LocalTime.of(9,0);
        LocalTime endHourToTest = LocalTime.of(15,0);

        Shift shift1 = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build();
        Shift shift2 = new TestShiftBuilder().withStartHour(LocalTime.of(15, 0)).withEndHour(LocalTime.of(20, 0)).build();
        Shift shift3 = new TestShiftBuilder().withStartHour(startHourToTest).withEndHour(endHourToTest).build();

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
        shiftRepository.save(shift3);

        //when
        boolean shouldExist = shiftServiceImpl.exists(startHourToTest, endHourToTest);
        boolean shouldNotExist = shiftServiceImpl.exists(LocalTime.of(1, 0), LocalTime.of(22, 0));

        //then
        assertTrue(shouldExist);
        assertFalse(shouldNotExist);
    }

    @Test
    void existsByHours_endHourIsBeforeStartHourShouldThrowException(){
        //given
        LocalTime startHour = LocalTime.of(20,0);
        LocalTime endHour = LocalTime.of(10,0);

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.exists(startHour, endHour));

        //then
        assertEquals("End hour cannot be before start hour",exception.getMessage());
    }

    @Test
    void existsById_workingTest(){
        //given
        Shift shift1 = new TestShiftBuilder().withStartHour(LocalTime.of(8, 0)).withEndHour(LocalTime.of(14, 0)).build();
        Shift shift2 = new TestShiftBuilder().withStartHour(LocalTime.of(15, 0)).withEndHour(LocalTime.of(20, 0)).build();
        Shift shift3 = new TestShiftBuilder().withStartHour(LocalTime.of(9, 0)).withEndHour(LocalTime.of(15, 0)).build();

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
        shiftRepository.save(shift3);

        //when
        boolean response1 = shiftServiceImpl.exists(shift1.getId());
        boolean response2 = shiftServiceImpl.exists(shift2.getId());
        boolean response3 = shiftServiceImpl.exists(shift3.getId());
        boolean shouldNotExist = shiftServiceImpl.exists(555L);

        //then
        assertTrue(response1);
        assertTrue(response2);
        assertTrue(response3);

        assertFalse(shouldNotExist);
    }

    @Test
    void getLength_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(8,0);
        LocalTime endHour = LocalTime.of(14,0);
        int expectedLength = 6;

        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        Integer serviceResponse = shiftServiceImpl.getLength(shiftHoursDTO);

        //then
        assertEquals(expectedLength,serviceResponse);
    }

    @Test
    void getLength_dtoIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> shiftServiceImpl.getLength(null));
        //then
    }

    @Test
    void getLength_argumentInsideDtoIsNullThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(null).build();

        //when
        assertThrows(ConstraintViolationException.class, () -> shiftServiceImpl.getLength(shiftHoursDTO));
        //then
    }

    @Test
    void getLength_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(20,0);
        LocalTime endHour = LocalTime.of(10,0);
        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        //when
        assertThrows(IllegalArgumentException.class, () -> shiftServiceImpl.getLength(shiftHoursDTO));

        //then
    }

    @Test
    void getDurationHours_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(12,0);
        LocalTime endHour = LocalTime.of(22,0);
        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        BigDecimal expectedDuration = BigDecimal.valueOf(10L);

        //when
        BigDecimal serviceResponse = shiftServiceImpl.getDurationHours(shiftHoursDTO);

        //then
        assertEquals(expectedDuration,serviceResponse);
    }

    @Test
    void getDuration_dtoIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> shiftServiceImpl.getLength(null));
        //then
    }

    @Test
    void getShiftAsArray_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(12,0);
        LocalTime endHour = LocalTime.of(22,0);
        ShiftHoursDTO shiftHoursDTO = new TestShiftHoursDTO().withStartHour(startHour).withEndHour(endHour).build();

        int[] expectedArray = {0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0};

        //when
        int[] serviceResponse = shiftServiceImpl.getShiftAsArray(shiftHoursDTO);

        //then
        assertArrayEquals(expectedArray,serviceResponse);
    }

    @Test
    void getShiftAsArray_dtoIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> shiftServiceImpl.getLength(null));
        //then
    }
}