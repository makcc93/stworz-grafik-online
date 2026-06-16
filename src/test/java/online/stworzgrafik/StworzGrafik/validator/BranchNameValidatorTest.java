package online.stworzgrafik.StworzGrafik.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class BranchNameValidatorTest {
    @InjectMocks
    private BranchNameValidator branchNameValidator;

    @Test
    void validate_nameToUpperCaseTest(){
        //given
        String givenName = "warszawa";
        String expectedName = "WARSZAWA";

        //when
        String serviceResponse = branchNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowNumberIsNameTest(){
        //given
        String givenName = "WARSZAWA123";

        //when
        String serviceResponse = branchNameValidator.validate(givenName);

        //then
        assertEquals(givenName,serviceResponse);
    }

    @Test
    void validate_deleteAllSpacesTest(){
        //given
        String givenName = "          WARSZAWA 123           " ;
        String expectedName = "WARSZAWA 123";

        //when
        String serviceResponse = branchNameValidator.validate(givenName);

        //then
        assertEquals(expectedName,serviceResponse);
    }

    @Test
    void validate_allowPolishAlphabetCharsTest(){
        //given
        String name = "ĄĘĆŁŃÓŚŹŻ";

        //when
        String serviceResponse = branchNameValidator.validate(name);

        //then
        assertEquals(name, serviceResponse);
    }

    @Test
    void getSupportedType_workingTest(){
        //given
        ObjectType positionType = ObjectType.BRANCH;
        ObjectType otherType = ObjectType.REGION;

        //when
        ObjectType supportedType = branchNameValidator.getSupportedType();

        //then
        assertEquals(positionType,supportedType);
        assertNotEquals(otherType,supportedType);
    }

}