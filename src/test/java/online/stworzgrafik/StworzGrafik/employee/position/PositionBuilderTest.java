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

    @Test
    void createPosition_deletingWhiteSpacesAndAllowSpacesInsideText(){
        //given
        String name = "     STORE MANAGER";
        String description = null;
        String expectedResponseName = "STORE MANAGER";

        //when
        Position postion = new PositionBuilder().createPosition(name, description);

        //then
        assertEquals(expectedResponseName,postion.getName());
    }

    @Test
    void createPosition_upperCaseTest(){
        //given
        String name = "manager";
        String description = null;
        String expectedResponseName = "MANAGER";

        //when
        Position postion = new PositionBuilder().createPosition(name,description);

        //then
        assertEquals(expectedResponseName,postion.getName());
    }

    @Test
    void createPosition_illegalCharsThrowsException(){
        //given
        String name = "!@#$%^&*()123";
        String description = null;

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> new PositionBuilder().createPosition(name, description));

        //then
        assertEquals("Name cannot contains illegal chars", exception.getMessage());
    }
}