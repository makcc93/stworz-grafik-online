package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    //w pierwszej kolejnosci zrob:
    // 1: usun wszytskie walidacje w dto (zostaw @notnull, @notblank), builderach -> przenies calosc do serviceImpl tam ma byc sprwadanie
    // 2: potem kontynuuj budowanie kolejnych NameValidatorStategy -> na razie masz BranchNameValidator, a potrzeba kolejne
}