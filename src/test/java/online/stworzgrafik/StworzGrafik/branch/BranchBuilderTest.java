package online.stworzgrafik.StworzGrafik.branch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BranchBuilderTest {

    @Test
    void create_workingTest(){
        //given
        String name = "Testing branch";

        //when
        Branch branch = new BranchBuilder().createBranch(name);

        //then
        assertEquals(name.toUpperCase(), branch.getName());
    }

    @Test
    void create_argumentIsNull(){
        //given
        String nullName = null;

        //when

        NullPointerException exception = assertThrows(NullPointerException.class, () -> new BranchBuilder().createBranch(nullName));

        //then
        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void create_upperCaseAndNameTrimTest(){
        //given
        String name = "              tEsT             ";

        //when
        Branch branch = new BranchBuilder().createBranch(name);

        //then
        assertEquals("TEST", branch.getName());
    }
}