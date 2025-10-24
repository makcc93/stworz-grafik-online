package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class StoreNameValidatorTest {
    @InjectMocks
    private StoreNameValidator storeNameValidator;

    @Test
    void validate_toUpperCaseTest(){
        //given
        String givenName = "Store";
        String expectedName = "STORE";

        //when
        String serviceResponse = storeNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_illegalCharsThrowsException(){
        //given
        String name = "!@#$%^&*(){}";

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> storeNameValidator.validate(name));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void validate_nameCanContainsDashTest(){
        //given
        String name = "REAL-ONE-STORE";

        //when
        String serviceResponse = storeNameValidator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void validate_polishAlphabetDoesNotThrowException(){
        //given
        String name = "ĄĘĆŁŃÓŚŹŻ";

        //when
        String serviceResponse = storeNameValidator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void validate_deleteSpacesBeforeAndAfterName(){
        //given
        String givenName = "            STORE             ";
        String expectedName = "STORE";

        //when
        String serviceResponse = storeNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowSpacesInsideName(){
        //given
        String name = "BEST STORE EVER";

        //when
        String serviceResponse = storeNameValidator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void getObjectType_workingTest(){
        //given
        ObjectType objectTypeStore = ObjectType.STORE;
        ObjectType otherType = ObjectType.POSITION;

        //when
        ObjectType supportedType = storeNameValidator.getSupportedType();

        //then
        assertEquals(objectTypeStore,supportedType);
        assertNotEquals(otherType,supportedType);
    }
}