package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestCreateBranchDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestUpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {
    @InjectMocks
    BranchServiceImpl branchService;

    @Mock
    BranchRepository branchRepository;

    @Mock
    BranchBuilder branchBuilder;

    @Mock
    BranchMapper branchMapper;

    @Mock
    RegionRepository regionRepository;

    @Test
    void findById_workingTest(){
        //given
        String name = "Test";
        Long id = 1L;

        Branch branch = new TestBranchBuilder().withName(name).build();

        when(branchRepository.findById(id)).thenReturn(Optional.ofNullable(branch));

        ResponseBranchDTO responseBranchDTO = new TestResponseBranchDTO().withId(id).withName(name).build();
        when(branchMapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO response = branchService.findById(id);

        //then
        assertEquals(id,response.id());
        assertEquals(name, response.name());
        assertTrue(response.isEnable());

        verify(branchRepository,times(1)).findById(id);
    }

    @Test
    void findById_entityNotFoundById(){
        //given
        Long id = 1L;

        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchService.findById(id));

        //then
        assertEquals("Branch with id " + id + " does not exist",exception.getMessage());
    }

    @Test
    void findById_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> branchService.findById(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());
    }

    @Test
    void createBranch_workingTest(){
        //given
        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().build();
        Long id = 1L;
        boolean isEnable = true;

        Region region = new TestRegionBuilder().build();
        when(regionRepository.findById(any())).thenReturn(Optional.of(region));

        when(branchRepository.existsByName(createBranchDTO.name())).thenReturn(false);

        Branch branch = new TestBranchBuilder().withName(createBranchDTO.name()).build();
        when(branchBuilder.createBranch(createBranchDTO.name(),region)).thenReturn(branch);

        when(branchRepository.save(branch)).thenReturn(branch);

        ResponseBranchDTO responseBranchDTO = new TestResponseBranchDTO().build();
        when(branchMapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO serviceResponse = branchService.createBranch(createBranchDTO);

        //then
        assertEquals(id,serviceResponse.id());
        assertEquals(responseBranchDTO.name(),serviceResponse.name());
        assertEquals(isEnable,serviceResponse.isEnable());

        verify(branchRepository,times(1)).save(any(Branch.class));
    }

    @Test
    void createBranch_branchAlreadyExistsThrowsException(){
        //given
        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().build();
        when(branchRepository.existsByName(createBranchDTO.name())).thenReturn(true);

        //when
        EntityExistsException entityExistsException = assertThrows(EntityExistsException.class, () -> branchService.createBranch(createBranchDTO));

        //then
        assertEquals("Branch with name " + createBranchDTO.name() + " already exists",entityExistsException.getMessage());
    }

    @Test
    void createBranch_argumentDTOisNullThrowsException(){
        //given
        CreateBranchDTO createBranchDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> branchService.createBranch(createBranchDTO));

        //then
        verify(branchRepository,never()).existsByName(any());
        verify(branchRepository,never()).save(any());
    }

    @Test
    void updateBranch_workingTest(){
        //given
        Long id = 1L;
        boolean isEnable = false;
        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().withIsEnable(isEnable).build();

        Branch branch = new TestBranchBuilder().withName("Test").build();
        branch.setEnable(true);
        when(branchRepository.findById(id)).thenReturn(Optional.ofNullable(branch));

        when(branchRepository.save(branch)).thenReturn(branch);

        ResponseBranchDTO responseBranchDTO =
                new TestResponseBranchDTO().withId(id).withName(updateBranchDTO.name()).withIsEnable(isEnable).build();
        when(branchMapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO serviceResponse = branchService.updateBranch(id, updateBranchDTO);

        //then
        assertEquals(id,responseBranchDTO.id());
        assertEquals(updateBranchDTO.name(),serviceResponse.name());
        assertFalse(responseBranchDTO.isEnable());

        verify(branchRepository,times(1)).findById(id);
        verify(branchRepository,times(1)).save(any(Branch.class));
    }

    @Test
    void updateBranch_branchDoesNotExistThrowsException(){
        //given
        Long id = 1L;
        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().build();
        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchService.updateBranch(id, updateBranchDTO));

        //then
        assertEquals("Branch with id " + id + " does not exist", exception.getMessage());
    }

    @Test
    void updateBranch_idArgumentIsNullThrowsException(){
        //given
        Long id = null;
        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().build();

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> branchService.updateBranch(id, updateBranchDTO));

        //then
        assertEquals("Id cannot be null",exception.getMessage());

        verify(branchRepository,never()).findById(any());
        verify(branchRepository,never()).save(any());
    }

    @Test
    void updateBranch_DTOIsNullThrowsException(){
        //given
        Long id = 1L;
        UpdateBranchDTO updateBranchDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> branchService.updateBranch(id, updateBranchDTO));

        //then
        verify(branchRepository,never()).findById(any());
        verify(branchRepository,never()).save(any());
    }

    @Test
    void delete_workingTest(){
        //given
        Long id = 1L;

        when(branchRepository.existsById(id)).thenReturn(true);

        //when
        branchService.delete(id);

        //then
        verify(branchRepository,times(1)).deleteById(id);
    }

    @Test
    void delete_entityDoesNotExistThrowsException(){
        //given
        Long id = 1L;

        when(branchRepository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchService.delete(id));

        //then
        assertEquals("Branch with id " + id + " does not exist",exception.getMessage());

        verify(branchRepository,times(1)).existsById(id);
        verify(branchRepository,never()).deleteById(any());
    }

    @Test
    void delete_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> branchService.delete(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(branchRepository,never()).existsById(any());
        verify(branchRepository,never()).save(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1L;
        when(branchRepository.existsById(id)).thenReturn(true);

        //when
        branchService.exists(id);

        //then
        verify(branchRepository,times(1)).existsById(id);
    }

    @Test
    void existsById_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> branchService.exists(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(branchRepository,never()).existsById(any());
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "Test";
        when(branchRepository.existsByName(name)).thenReturn(true);

        //when
        branchService.exists(name);

        //then
        verify(branchRepository,times(1)).existsByName(name);
    }

    @Test
    void existsByName_nameIsNullThrowsException(){
        //given
        String name = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> branchService.exists(name));

        //then
        assertEquals("Name cannot be null", exception.getMessage());

        verify(branchRepository,never()).existsByName(any());
    }

    @Test
    void findAll_workingTest(){
        //given
        Branch branch1 = new TestBranchBuilder().withName("FIRST").build();
        Branch branch2 = new TestBranchBuilder().withName("SECOND").build();
        List<Branch> branches = List.of(branch1,branch2);

        ResponseBranchDTO responseBranch1 = new TestResponseBranchDTO().withId(1L).withName(branch1.getName()).build();
        ResponseBranchDTO responseBranch2 = new TestResponseBranchDTO().withId(2L).withName(branch2.getName()).build();
        List<ResponseBranchDTO> dtos = List.of(responseBranch1,responseBranch2);

        when(branchRepository.findAll()).thenReturn(branches);
        when(branchMapper.toResponseBranchDTO(branch1)).thenReturn(responseBranch1);
        when(branchMapper.toResponseBranchDTO(branch2)).thenReturn(responseBranch2);

        //when
        List<ResponseBranchDTO> branchDTOS = branchService.findAll();

        //then
        assertTrue(branchDTOS.containsAll(dtos));

        verify(branchRepository,times(1)).findAll();
    }

    @Test
    void findAll_emptyListWorkingTest(){
        //given
        when(branchRepository.findAll()).thenReturn(new ArrayList<>());

        //when
        List<ResponseBranchDTO> branchDTOS = branchService.findAll();

        //then
        assertEquals(0,branchDTOS.size());

        verify(branchRepository,times(1)).findAll();
    }
}