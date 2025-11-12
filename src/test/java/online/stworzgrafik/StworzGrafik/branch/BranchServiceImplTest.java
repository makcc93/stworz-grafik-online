package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
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
    private BranchServiceImpl branchServiceImpl;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchBuilder branchBuilder;

    @Mock
    private BranchMapper branchMapper;

    @Mock
    private RegionService regionService;

    @Mock
    private RegionEntityService regionEntityService;

    @Mock
    private NameValidatorService nameValidatorService;

    @Mock
    private EntityManager entityManager;

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
        ResponseBranchDTO response = branchServiceImpl.findById(id);

        //then
        assertEquals(id,response.id());
        assertEquals(name, response.name());
        assertTrue(response.enable());

        verify(branchRepository,times(1)).findById(id);
    }

    @Test
    void findById_entityNotFoundById(){
        //given
        Long id = 1L;

        when(branchRepository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchServiceImpl.findById(id));

        //then
        assertEquals("Branch with id " + id + " does not exist",exception.getMessage());
    }

    @Test
    void createBranch_workingTest(){
        //given
        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().build();
        Long id = 1L;
        boolean isEnable = true;

        when(branchRepository.existsByName(createBranchDTO.name())).thenReturn(false);

        when(nameValidatorService.validate(createBranchDTO.name(), ObjectType.BRANCH)).thenReturn(createBranchDTO.name());

        Region region = new TestRegionBuilder().build();
        when(regionEntityService.getEntityById(createBranchDTO.regionId())).thenReturn(region);

        Branch branch = new TestBranchBuilder().withName(createBranchDTO.name()).withRegion(region).build();
        when(branchBuilder.createBranch(createBranchDTO.name(),region)).thenReturn(branch);

        when(branchRepository.save(branch)).thenReturn(branch);

        ResponseBranchDTO responseBranchDTO = new TestResponseBranchDTO().build();
        when(branchMapper.toResponseBranchDTO(branch)).thenReturn(responseBranchDTO);

        //when
        ResponseBranchDTO serviceResponse = branchServiceImpl.createBranch(createBranchDTO);

        //then
        assertEquals(id,serviceResponse.id());
        assertEquals(responseBranchDTO.name(),serviceResponse.name());
        assertEquals(isEnable,serviceResponse.enable());

        verify(branchRepository,times(1)).save(any(Branch.class));
    }

    @Test
    void createBranch_regionDoesNotExistThrowsException(){
        //given
        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().build();
        when(regionEntityService.getEntityById(createBranchDTO.regionId()))
                .thenThrow(new EntityNotFoundException("Cannot find region by id " + createBranchDTO.regionId()));

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> branchServiceImpl.createBranch(createBranchDTO));

        //then
        assertEquals("Cannot find region by id " + createBranchDTO.regionId(),exception.getMessage());
        verify(entityManager,never()).getReference(any(),any());

    }

    @Test
    void createBranch_branchAlreadyExistsThrowsException(){
        //given
        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().build();
        when(branchRepository.existsByName(createBranchDTO.name())).thenReturn(true);

        //when
        EntityExistsException entityExistsException = assertThrows(EntityExistsException.class, () -> branchServiceImpl.createBranch(createBranchDTO));

        //then
        assertEquals("Branch with name " + createBranchDTO.name() + " already exist",entityExistsException.getMessage());
    }

    @Test
    void createBranch_argumentDTOisNullThrowsException(){
        //given
        CreateBranchDTO createBranchDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> branchServiceImpl.createBranch(createBranchDTO));

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
        ResponseBranchDTO serviceResponse = branchServiceImpl.updateBranch(id, updateBranchDTO);

        //then
        assertEquals(id,responseBranchDTO.id());
        assertEquals(updateBranchDTO.name(),serviceResponse.name());
        assertFalse(responseBranchDTO.enable());

        verify(branchMapper,times(1)).updateBranchFromDTO(updateBranchDTO,branch);
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
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchServiceImpl.updateBranch(id, updateBranchDTO));

        //then
        assertEquals("Branch with id " + id + " does not exist", exception.getMessage());
    }

    @Test
    void save_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().build();
        when(branchRepository.save(branch)).thenReturn(branch);

        //when
        Branch serviceResponse = branchServiceImpl.saveEntity(branch);

        //then
        assertEquals(branch.getName(),serviceResponse.getName());
    }

    @Test
    void delete_workingTest(){
        //given
        Long id = 1L;

        when(branchRepository.existsById(id)).thenReturn(true);

        //when
        branchServiceImpl.delete(id);

        //then
        verify(branchRepository,times(1)).deleteById(id);
    }

    @Test
    void delete_entityDoesNotExistThrowsException(){
        //given
        Long id = 1L;

        when(branchRepository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchServiceImpl.delete(id));

        //then
        assertEquals("Branch with id " + id + " does not exist",exception.getMessage());

        verify(branchRepository,times(1)).existsById(id);
        verify(branchRepository,never()).deleteById(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 1L;
        when(branchRepository.existsById(id)).thenReturn(true);

        //when
        branchServiceImpl.exists(id);

        //then
        verify(branchRepository,times(1)).existsById(id);
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "Test";
        when(branchRepository.existsByName(name)).thenReturn(true);

        //when
        branchServiceImpl.exists(name);

        //then
        verify(branchRepository,times(1)).existsByName(name);
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
        List<ResponseBranchDTO> branchDTOS = branchServiceImpl.findAll();

        //then
        assertTrue(branchDTOS.containsAll(dtos));

        verify(branchRepository,times(1)).findAll();
    }

    @Test
    void findAll_emptyListWorkingTest(){
        //given
        when(branchRepository.findAll()).thenReturn(new ArrayList<>());

        //when
        List<ResponseBranchDTO> branchDTOS = branchServiceImpl.findAll();

        //then
        assertEquals(0,branchDTOS.size());

        verify(branchRepository,times(1)).findAll();
    }
}