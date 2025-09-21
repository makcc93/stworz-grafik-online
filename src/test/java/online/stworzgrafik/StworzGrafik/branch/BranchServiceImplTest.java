package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

        when(repository.findById(id)).thenThrow(EntityNotFoundException.class);

        //when
        assertThrows(EntityNotFoundException.class, () -> service.findById(id));

        //then
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

}