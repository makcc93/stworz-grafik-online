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
    void toResponseBranchDTO_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().withName("Test").build();
        branch.setEnable(true);

        //when
        ResponseBranchDTO responseBranchDTO = branchMapper.toResponseBranchDTO(branch);

        //then
        assertEquals(branch.getId(),responseBranchDTO.id());
        assertEquals(branch.getName(),responseBranchDTO.name());
        assertTrue(responseBranchDTO.enable());
    }

    @Test
    void updateBranchFromDTO_workingTest() {
        //given
        Branch branch = new TestBranchBuilder().withName("Test").build();

        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().withIsEnable(false).build();

        //when
        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        //then
        assertEquals(updateBranchDTO.name(), branch.getName());
        assertFalse(branch.isEnable());
    }
}