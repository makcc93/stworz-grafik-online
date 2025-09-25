package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.parser.Entity;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShiftServiceImplIT {

    @Autowired
    ShiftRepository shiftRepository;

    @Autowired
    ShiftService shiftService;

    @Test
    void saveDto_workingTest(){
        //given
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(8,0),LocalTime.of(20,0));

        //when
        ResponseShiftDTO responseShiftDTO = shiftService.saveDto(shiftHoursDTO);

        //then
        assertEquals(8,responseShiftDTO.startHour().getHour());
        assertEquals(20,responseShiftDTO.endHour().getHour());
        assertEquals(12,responseShiftDTO.length());
        assertTrue(shiftRepository.existsById(responseShiftDTO.id()));
    }

    @Test
    void saveDto_negativeTest(){
        //given
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(8,0),LocalTime.of(20,0));

        //when
        ResponseShiftDTO responseShiftDTO = shiftService.saveDto(shiftHoursDTO);

        //then
        assertNotEquals(0,responseShiftDTO.startHour().getHour());
        assertNotEquals(24,responseShiftDTO.endHour().getHour());
        assertNotEquals(2,responseShiftDTO.length());
        assertFalse(shiftRepository.existsById(responseShiftDTO.id()+999));
    }

    @Test
    void saveDto_wrongHoursThrowException(){
        //given
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(23,0),LocalTime.of(6,0));

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shiftService.saveDto(shiftHoursDTO));

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
        Shift savedEntity = shiftService.saveEntity(shift);

        //then
        assertEquals(startHour.getHour(),savedEntity.startHour.getHour());
        assertEquals(endHour.getHour(),savedEntity.endHour.getHour());
        assertEquals(hoursDifference,savedEntity.getLength());

        assertTrue(shiftRepository.existsById(savedEntity.getId()));
    }

    @Test
    void create_workingTest(){
        //given
        ShiftHoursDTO shiftHoursDTO = new ShiftHoursDTO(LocalTime.of(15,0),LocalTime.of(20,0));

        //when
        ResponseShiftDTO responseShiftDTO = shiftService.create(shiftHoursDTO);

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
        assertThrows(NullPointerException.class,() -> shiftService.create(shiftHoursDTO));

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
        Shift entityById = shiftService.findEntityById(shift.getId());

        //then
        assertTrue(shiftRepository.existsById(entityById.getId()));
        assertEquals(startHour.getHour(),entityById.getStartHour().getHour());
        assertEquals(endHour.getHour(),entityById.getEndHour().getHour());
        assertEquals(hoursDifference,entityById.getLength());
    }

    @Test
    void findEntityById_notExistingEntityTest(){
        //given
        Long id = 1234L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftService.findEntityById(id));

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
        shiftService.delete(shiftToDelete.getId());

        //then
        assertFalse(shiftRepository.existsById(shiftToDelete.getId()));
        assertTrue(shiftRepository.existsById(shiftToLeave.getId()));
    }

    @Test
    void delete_entityNotExistException(){
        //given
        Long id = 999L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftService.delete(id));

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
        List<ResponseShiftDTO> all = shiftService.findAll();

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
        List<ResponseShiftDTO> all = shiftService.findAll();

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
        ResponseShiftDTO responseShiftDTO = shiftService.findById(shift2.getId());

        //then
        assertEquals(shift2.getId(),responseShiftDTO.id());
        assertTrue(shiftRepository.existsById(shift2.getId()));
    }

    @Test
    void findById_emptyListThrowException(){
        //given
        Long id = 52L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> shiftService.findById(id));

        //then
        assertEquals("Cannot find shift by id: " + id,exception.getMessage());
        assertFalse(shiftRepository.existsById(id));
    }

    @Test
    void existsByHours_workingTest(){
        //given
        LocalTime startHourToTest = LocalTime.of(9,0);
        LocalTime endHourToTest = LocalTime.of(15,0);

        Shift shift1 = new ShiftBuilder().createShift(LocalTime.of(8, 0), LocalTime.of(14, 0));
        Shift shift2 = new ShiftBuilder().createShift(LocalTime.of(15, 0), LocalTime.of(20, 0));
        Shift shift3 = new ShiftBuilder().createShift(startHourToTest, endHourToTest);

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
        shiftRepository.save(shift3);

        //when
        boolean shouldExist = shiftService.exists(startHourToTest, endHourToTest);
        boolean shouldNotExist = shiftService.exists(LocalTime.of(1, 0), LocalTime.of(22, 0));

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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shiftService.exists(startHour, endHour));

        //then
        assertEquals("End hour cannot be before start hour",exception.getMessage());
    }

    @Test
    void existsById_workingTest(){
        //given
        Shift shift1 = new ShiftBuilder().createShift(LocalTime.of(8, 0), LocalTime.of(14, 0));
        Shift shift2 = new ShiftBuilder().createShift(LocalTime.of(15, 0), LocalTime.of(20, 0));
        Shift shift3 = new ShiftBuilder().createShift(LocalTime.of(9, 0), LocalTime.of(15, 0));

        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
        shiftRepository.save(shift3);

        //when
        boolean doExistShift1 = shiftService.exists(shift1.id);
        boolean doExistShift2 = shiftService.exists(shift2.id);
        boolean doExistShift3 = shiftService.exists(shift3.id);
        boolean shouldNotExist = shiftService.exists(555L);

        //then
        assertTrue(doExistShift1);
        assertTrue(doExistShift2);
        assertTrue(doExistShift3);

        assertFalse(shouldNotExist);
    }
}