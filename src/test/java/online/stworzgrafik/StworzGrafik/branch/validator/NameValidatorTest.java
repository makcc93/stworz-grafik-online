package online.stworzgrafik.StworzGrafik.branch.validator;


import jakarta.validation.ValidationException;
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
        assertEquals("Branch name cannot contains illegal chars", validationException.getMessage());
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
}