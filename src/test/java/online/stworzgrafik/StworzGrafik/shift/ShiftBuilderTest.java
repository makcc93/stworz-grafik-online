package online.stworzgrafik.StworzGrafik.shift;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ShiftBuilderTest {

    @Test
    void createShift_workingTest(){
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(15,0);

        Shift shift = new ShiftBuilder().createShift(startHour,endHour);

        assertEquals(10,shift.startHour.getHour());
        assertEquals(15,shift.endHour.getHour());
    }

    @Test
    void createShift_startHourIsNull(){
        LocalTime startHour = null;
        LocalTime endHour = LocalTime.of(15,0);
        ShiftBuilder shiftBuilder = new ShiftBuilder();

        assertThrows(NullPointerException.class, () -> shiftBuilder.createShift(startHour, endHour));
    }

    @Test
    void createShift_endHourIsNull(){
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = null;
        ShiftBuilder shiftBuilder = new ShiftBuilder();

        assertThrows(NullPointerException.class, () -> shiftBuilder.createShift(startHour, endHour));
    }

    @Test
    void createShift_bothArgumentsAreNull(){
        ShiftBuilder shiftBuilder = new ShiftBuilder();

        assertThrows(NullPointerException.class, () -> shiftBuilder.createShift(null, null));
    }

    @Test
    void createShift_endHourIsBeforeStartHourThrowException(){
        //given
        LocalTime startHour = LocalTime.of(15,0);
        LocalTime endHour = LocalTime.of(10,0);
        ShiftBuilder shiftBuilder = new ShiftBuilder();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shiftBuilder.createShift(startHour, endHour));

        //then
        assertEquals("End hour cannot be before start hour",exception.getMessage());
    }

    @Test
    void shiftBuilder_constructorDoesNotThrowException(){
        //given
        //when
        //then
        assertDoesNotThrow(ShiftBuilder::new);
    }
}