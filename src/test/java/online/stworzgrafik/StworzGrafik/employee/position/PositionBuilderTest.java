package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionBuilderTest {
    @Test
    void createPosition_workingTest(){
        //given
        String name = "MANAGER";
        String description = null;

        //when
        Position postion = new PositionBuilder().createPosition(name,description);

        //then
        assertEquals(name,postion.getName());
    }
}