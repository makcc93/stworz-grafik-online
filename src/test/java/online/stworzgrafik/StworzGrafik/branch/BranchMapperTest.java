package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchMapper;
import online.stworzgrafik.StworzGrafik.branch.BranchMapperImpl;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BranchMapperTest {

    @Autowired
    private final BranchMapper branchMapper = new BranchMapperImpl();

    @Autowired
    private BranchService branchService;

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
  
}