package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BranchBuilderTest {

    @Test
    void create_workingTest(){
        //given
        String name = "Testing branch";

        //when
        Branch branch = new TestBranchBuilder().withName(name).build();

        //then
        assertEquals("TESTINGBRANCH", branch.getName());
    }

    @Test
    void create_argumentIsNull(){
        //given
        String nullName = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new TestBranchBuilder().withName(nullName).build());

        //then
        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void create_validationNameTest(){
        //given
        String name = "              t EsT             ";

        //when
        Branch branch = new TestBranchBuilder().withName(name).build();

        //then
        assertEquals("TEST", branch.getName());
    }

    @Test
    void create_illegalCharsInNameThrowsException(){
        //given
        String name = "!!!!@@@@%^&*()";

        //when
        ValidationException validationException = assertThrows(ValidationException.class, () -> new TestBranchBuilder().withName(name).build());

        //then
        assertEquals("Name cannot contains illegal chars", validationException.getMessage());
    }

    @Test
    void branchBuilderDoesNotThrowException(){
        //given
        //when
        //then
        assertDoesNotThrow(TestBranchBuilder::new);
    }
}