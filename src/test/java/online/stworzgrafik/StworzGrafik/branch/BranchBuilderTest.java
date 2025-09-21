package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BranchBuilderTest {

    @Test
    void create_workingTest(){
        //given
        String name = "Testing branch";
        BranchBuilder branchBuilder = new BranchBuilder();

        //when
        Branch branch = branchBuilder.createBranch(name);

        //then
        assertEquals("TESTINGBRANCH", branch.getName());
    }

    @Test
    void create_argumentIsNull(){
        //given
        String nullName = null;
        BranchBuilder branchBuilder = new BranchBuilder();

        //when

        NullPointerException exception = assertThrows(NullPointerException.class, () -> branchBuilder.createBranch(nullName));

        //then
        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void create_validationNameTest(){
        //given
        String name = "              t EsT             ";
        BranchBuilder branchBuilder = new BranchBuilder();

        //when
        Branch branch = branchBuilder.createBranch(name);

        //then
        assertEquals("TEST", branch.getName());
    }

    @Test
    void create_illegalCharsInNameThrowsException(){
        //given
        String name = "!!!!@@@@%^&*()";
        BranchBuilder branchBuilder = new BranchBuilder();

        //when
        ValidationException validationException = assertThrows(ValidationException.class, () -> branchBuilder.createBranch(name));

        //then
        assertEquals("Branch name cannot contains illegal chars", validationException.getMessage());
    }

    @Test
    void branchBuilderDoesNotThrowException(){
        //given
        //when
        //then
        assertDoesNotThrow(BranchBuilder::new);
    }
}