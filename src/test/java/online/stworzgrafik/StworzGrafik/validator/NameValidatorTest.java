package online.stworzgrafik.StworzGrafik.validator;


import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.validator.NameValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameValidatorTest {

    @Test
    void validate_workingTest(){
        //given
        String name = "Test6";

        //when
        String validated = NameValidator.validate(name);

        //then
        assertEquals("TEST6",validated);
    }

    @Test
    void validate_illegalCharThrowsException() {
        //given
        String name = "Test!@#$%^&*()";

        //when
        ValidationException validationException = assertThrows(jakarta.validation.ValidationException.class, () -> NameValidator.validate(name));

        //then
        assertEquals("Name cannot contains illegal chars", validationException.getMessage());
    }

    @Test
    void validate_deletingWhiteSpaces() {
        //given
        String name = "       I \n L O V E    \t\n\t\n programming";

        //when
        String validated = NameValidator.validate(name);

        //then
        assertEquals("ILOVEPROGRAMMING", validated);
    }

    @Test
    void validate_nameIsNullThrowsException(){
        //given
        String name = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> NameValidator.validate(name));

        //then
        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void validateForPositon_wokingTest(){
        //given
        String name = "kasjer";

        //when
        String response = NameValidator.validateForPosition(name);

        //then
        assertEquals("KASJER",response);
    }

    @Test
    void validateForPosition_removingWhiteSpaces(){
        //given
        String name = "                 doradca klienta          ";

        //when
        String response = NameValidator.validateForPosition(name);

        //then
        assertEquals("DORADCA KLIENTA", response);
    }

    @Test
    void validateForPositon_numbersInNameThrowsException(){
        //given
        String name = "Magazynier 1";

        //when
        ValidationException exception = assertThrows(ValidationException.class, () -> NameValidator.validateForPosition(name));

        //then
        assertEquals("Name cannot contains illegal chars", exception.getMessage());
    }

    @Test
    void validateForPosition_illegalCharsThrowsException(){
        //given
        String name = "!@#$%^&*()";

        //when
        ValidationException exception = assertThrows(ValidationException.class, () -> NameValidator.validateForPosition(name));

        //then
        assertEquals("Name cannot contains illegal chars", exception.getMessage());
    }

    @Test
    void validateForPosition_nameIsNullThrowsException(){
        //given
        String name = null;
        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> NameValidator.validateForPosition(name));

        //then
        assertEquals("Name cannot be null", exception.getMessage());
    }
}