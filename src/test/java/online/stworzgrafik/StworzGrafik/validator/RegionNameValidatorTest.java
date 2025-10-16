package online.stworzgrafik.StworzGrafik.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class RegionNameValidatorTest {
    @InjectMocks
    private RegionNameValidator regionNameValidator;

    @Test
    void validate_nameToUpperCaseTest(){
        //given
        String givenName = "wschod";
        String expectedName = "WSCHOD";

        //when
        String serviceResponse = regionNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowNumbersIsNameTest(){
        //given
        String name = "WSCHOD123";

        //when
        String serviceResponse = regionNameValidator.validate(name);

        //then
        assertEquals(name,serviceResponse);
    }

    @Test
    void validate_deleteAllSpacesTest(){
        //given
        String givenName = " \t\n W      SC H O D 1 1 1 \n  " ;
        String expectedName = "WSCHOD111";

        //when
        String serviceResponse = regionNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowPolishAlphabetCharsTest(){
        //given
        String name = "ĄĘĆŁŃÓŚŹŻ";

        //when
        String serviceResponse = regionNameValidator.validate(name);

        //then
        assertEquals(name, serviceResponse);
    }

    @Test
    void getSupportedType_workingTest(){
        //given
        ObjectType positionType = ObjectType.REGION;
        ObjectType otherType = ObjectType.PERSON;

        //when
        ObjectType supportedType = regionNameValidator.getSupportedType();

        //then
        assertEquals(positionType,supportedType);
        assertNotEquals(otherType,supportedType);
    }


}