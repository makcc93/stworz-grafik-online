package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BranchBuilderTest {

    @Test
    void create_workingTest(){
        //given
        String name = "Testing branch";
        Region region = new TestRegionBuilder().build();

        //when
        Branch branch = new TestBranchBuilder().withName(name).withRegion(region).build();

        //then
        assertEquals(name, branch.getName());
        assertEquals(region.getName(),branch.getRegion().getName());
    }

    @Test
    void branchBuilderDoesNotThrowException(){
        //given
        //when
        //then
        assertDoesNotThrow(TestBranchBuilder::new);
    }
}