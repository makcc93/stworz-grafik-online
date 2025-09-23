package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.NameBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static online.stworzgrafik.StworzGrafik.dataFactory.TestDataFactory.defaultUpdateBranchDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {
    @InjectMocks
    BranchServiceImpl service;

    @Mock
    BranchRepository repository;

    @Mock
    BranchBuilder builder;

    @Mock
    BranchMapper mapper;

    @Test
    void findById_workingTest(){
        //given
        String name = "Test";
        Long id = 1L;

        Branch branch = new BranchBuilder().createBranch(name);

        when(repository.findById(id)).thenReturn(Optional.ofNullable(branch));

        ResponseBranchDTO responseBranchDTO = new ResponseBranchDTO(id, name, true);
        when(mapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO response = service.findById(id);

        //then
        assertEquals(id,response.id());
        assertEquals(name, response.name());
        assertTrue(response.isEnable());

        verify(repository,times(1)).findById(id);
    }

    @Test
    void findById_entityNotFoundById(){
        //given
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.findById(id));

        //then
        assertEquals("Branch with id " + id + " does not exist",exception.getMessage());
    }

    @Test
    void findById_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.findById(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());
    }

    @Test
    void createBranch_workingTest(){
        //given
        NameBranchDTO nameBranchDTO = new NameBranchDTO("Test");
        Long id = 1L;
        boolean isEnable = true;

        when(repository.existsByName(nameBranchDTO.name())).thenReturn(false);

        Branch branch = new BranchBuilder().createBranch(nameBranchDTO.name());
        when(builder.createBranch(nameBranchDTO.name())).thenReturn(branch);

        when(repository.save(branch)).thenReturn(branch);

        ResponseBranchDTO responseBranchDTO = new ResponseBranchDTO(id, nameBranchDTO.name(), isEnable);
        when(mapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO serviceResponse = service.createBranch(nameBranchDTO);

        //then
        assertEquals(id,serviceResponse.id());
        assertEquals("TEST",responseBranchDTO.name());
        assertEquals(isEnable,responseBranchDTO.isEnable());

        verify(repository,times(1)).save(any(Branch.class));
    }

    @Test
    void createBranch_branchAlreadyExistsThrowsException(){
        //given
        NameBranchDTO nameBranchDTO = new NameBranchDTO("Test");
        when(repository.existsByName(nameBranchDTO.name())).thenReturn(true);

        //when
        EntityExistsException entityExistsException = assertThrows(EntityExistsException.class, () -> service.createBranch(nameBranchDTO));

        //then
        assertEquals("Branch with name " + nameBranchDTO.name() + " already exists",entityExistsException.getMessage());
    }

    @Test
    void createBranch_argumentDTOisNullThrowsException(){
        //given
        NameBranchDTO nameBranchDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> service.createBranch(nameBranchDTO));

        //then
        verify(repository,never()).existsByName(any());
        verify(repository,never()).save(any());
    }

    @Test
    void updateBranch_workingTest(){
        //given
        Long id = 1L;
        boolean isEnable = false;
        UpdateBranchDTO updateBranchDTO = defaultUpdateBranchDTO();

        Branch branch = new BranchBuilder().createBranch("TEST");
        branch.setEnable(true);
        when(repository.findById(id)).thenReturn(Optional.ofNullable(branch));

        when(repository.save(branch)).thenReturn(branch);

        ResponseBranchDTO responseBranchDTO = new ResponseBranchDTO(id,updateBranchDTO.name(),isEnable);
        when(mapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO serviceResponse = service.updateBranch(id, updateBranchDTO);

        //then
        assertEquals(id,responseBranchDTO.id());
        assertEquals(updateBranchDTO.name(),serviceResponse.name());
        assertFalse(responseBranchDTO.isEnable());

        verify(repository,times(1)).findById(id);
        verify(repository,times(1)).save(any(Branch.class));
    }

    @Test
    void updateBranch_branchDoesNotExistThrowsException(){
        //given
        Long id = 1L;
        UpdateBranchDTO updateBranchDTO = defaultUpdateBranchDTO();
        when(repository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.updateBranch(id, updateBranchDTO));

        //then
        assertEquals("Branch with id " + id + " does not exist", exception.getMessage());
    }

    @Test
    void updateBranch_idArgumentIsNullThrowsException(){
        //given
        Long id = null;
        UpdateBranchDTO updateBranchDTO = defaultUpdateBranchDTO();

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.updateBranch(id, updateBranchDTO));

        //then
        assertEquals("Id cannot be null",exception.getMessage());

        verify(repository,never()).findById(any());
        verify(repository,never()).save(any());
    }

    @Test
    void updateBranch_DTOIsNullThrowsException(){
        //given
        Long id = 1L;
        UpdateBranchDTO updateBranchDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> service.updateBranch(id, updateBranchDTO));

        //then
        verify(repository,never()).findById(any());
        verify(repository,never()).save(any());
    }

    @Test
    void delete_workingTest(){
        //given
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(true);

        //when
        service.delete(id);

        //then
        verify(repository,times(1)).deleteById(id);
    }

    @Test
    void delete_entityDoesNotExistThrowsException(){
        //given
        Long id = 1L;

        when(repository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.delete(id));

        //then
        assertEquals("Branch with id " + id + " does not exist",exception.getMessage());

        verify(repository,times(1)).existsById(id);
        verify(repository,never()).deleteById(any());
    }

    @Test
    void delete_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.delete(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(repository,never()).existsById(any());
        verify(repository,never()).save(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);

        //when
        service.exists(id);

        //then
        verify(repository,times(1)).existsById(id);
    }

    @Test
    void existsById_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(repository,never()).existsById(any());
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "Test";
        when(repository.existsByName(name)).thenReturn(true);

        //when
        service.exists(name);

        //then
        verify(repository,times(1)).existsByName(name);
    }

    @Test
    void existsByName_nameIsNullThrowsException(){
        //given
        String name = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> service.exists(name));

        //then
        assertEquals("Name cannot be null", exception.getMessage());

        verify(repository,never()).existsByName(any());
    }

    @Test
    void findAll_workingTest(){
        //given
        Branch branch1 = new BranchBuilder().createBranch("FIRST");
        Branch branch2 = new BranchBuilder().createBranch("SECOND");
        List<Branch> branches = List.of(branch1,branch2);

        ResponseBranchDTO responseBranch1 = new ResponseBranchDTO(1L,branch1.getName(),true);
        ResponseBranchDTO responseBranch2 = new ResponseBranchDTO(2L,branch2.getName(),true);
        List<ResponseBranchDTO> dtos = List.of(responseBranch1,responseBranch2);

        when(repository.findAll()).thenReturn(branches);
        when(mapper.toResponseBranchDTO(branch1)).thenReturn(responseBranch1);
        when(mapper.toResponseBranchDTO(branch2)).thenReturn(responseBranch2);

        //when
        List<ResponseBranchDTO> branchDTOS = service.findAll();

        //then
        assertTrue(branchDTOS.containsAll(dtos));

        verify(repository,times(1)).findAll();
    }

    @Test
    void findAll_emptyListWorkingTest(){
        //given
        when(repository.findAll()).thenReturn(new ArrayList<>());

        //when
        List<ResponseBranchDTO> branchDTOS = service.findAll();

        //then
        assertEquals(0,branchDTOS.size());

        verify(repository,times(1)).findAll();
    }
}