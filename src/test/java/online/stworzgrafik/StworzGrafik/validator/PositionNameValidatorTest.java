package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PositionNameValidatorTest {
    @InjectMocks
    private PositionNameValidator positionNameValidator;

    @Test
    void validate_nameToUpperCaseTest(){
        //given
        String givenName = "manager";
        String expectedName = "MANAGER";

        //when
        String serviceResponse = positionNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowDashInNameTest(){
        //given
        String givenName = "MANAGER-SELLER";

        //when
        String serviceResponse = positionNameValidator.validate(givenName);

        //then
        assertEquals(givenName,serviceResponse);
    }

    @Test
    void validate_deleteWithSpacesInNameTest(){
        //given
        String givenName = "       MANAGER-SELLER         ";
        String expectedName = "MANAGER-SELLER";

        //when
        String serviceResponse = positionNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_illegalCharsThrowsException(){
        //given
        String name = " MANAGER!@$%^&*(){}";

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> positionNameValidator.validate(name));

        //then
        assertEquals("Name cannot contains illegal chars", exception.getMessage());
    }

    @Test
    void validate_allowSpacesBetweenNamesTest(){
        //given
        String givenName = "    STORE MANAGER   ";
        String expectedName = "STORE MANAGER";

        //when
        String serviceResponse = positionNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowPolishAlphabetCharsTest(){
        //given
        String name = "ĄĘĆŁŃÓŚŹŻ";

        //when
        String serviceResponse = positionNameValidator.validate(name);

        //then
        assertEquals(name, serviceResponse);
    }

    @Test
    void getSupportedType_workingTest(){
        //given
        ObjectType positionType = ObjectType.POSITION;
        ObjectType otherType = ObjectType.REGION;

        //when
        ObjectType supportedType = positionNameValidator.getSupportedType();

        //then
        assertEquals(positionType,supportedType);
        assertNotEquals(otherType,supportedType);
    }
}