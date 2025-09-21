package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BranchMapperTest {

    private final BranchMapper branchMapper = new BranchMapperImpl();

    @Test
    void toResponseBranchDTO(){
        //given
        Branch branch = new BranchBuilder().createBranch("Test");
        branch.setEnable(true);

        //when
        ResponseBranchDTO responseBranchDTO = branchMapper.toResponseBranchDTO(branch);

        //then
        assertEquals(branch.getId(),responseBranchDTO.id());
        assertEquals(branch.getName().toUpperCase(),responseBranchDTO.name());
        assertTrue(responseBranchDTO.isEnable());
    }

    @Test
    void updateBranchFromDTO_workingTest() {
        //given
        Branch branch = new BranchBuilder().createBranch("Test");

        UpdateBranchDTO updateBranchDTO = new UpdateBranchDTO("   Changed name   ", false);

        //when
        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        //then
        assertEquals("CHANGEDNAME", branch.getName());
        assertFalse(branch.isEnable());
    }
}