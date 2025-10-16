package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class PersonNameValidatorTest {
    @InjectMocks
    private PersonNameValidator validator;

    @Test
    void validate_toUpperCaseTest(){
        //given
        String givenName = "firstName";
        String expectedName = "FIRSTNAME";

        //when
        String serviceResponse = validator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_illegalCharsThrowsException(){
        //given
        String name = "!@#$%^&*(){}";

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> validator.validate(name));

        //then
        assertEquals("Name cannot contains illegal chars", exception.getMessage());
    }

    @Test
    void validate_nameCanContainsDashSign(){
        //given
        String name = "FIRST-NAME";

        //when
        String serviceResponse = validator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void validate_polishAlphabetDoesNotThrowException(){
        //given
        String name = "ĄĘĆŁŃÓŚŹŻ";

        //when
        String serviceResponse = validator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void validate_deleteSpacesBeforeAndAfterName(){
        //given
        String givenName = "            NAME             ";
        String expectedName = "NAME";

        //when
        String serviceResponse = validator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowSpacesInsideName(){
        //given
        String name = "LAST NAME";

        //when
        String serviceResponse = validator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void getObjectType_workingTest(){
        //given
        ObjectType objectTypeStore = ObjectType.PERSON;
        ObjectType otherType = ObjectType.BRANCH;

        //when
        ObjectType supportedType = validator.getSupportedType();

        //then
        assertEquals(objectTypeStore,supportedType);
        assertNotEquals(otherType,supportedType);
    }

}