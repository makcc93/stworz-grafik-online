package online.stworzgrafik.StworzGrafik.shift;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ShiftBuilderTest {

    @Test
    void createShift_workingTest(){
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(15,0);

        Shift shift = new TestShiftBuilder().withStartHour(startHour).withEndHour(endHour).build();

        assertEquals(10,shift.startHour.getHour());
        assertEquals(15,shift.endHour.getHour());
    }

    @Test
    void shiftBuilder_constructorDoesNotThrowException(){
        //given
        //when
        //then
        assertDoesNotThrow(ShiftBuilder::new);
    }
}