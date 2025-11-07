package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BranchServiceIT {

    @Autowired
    private BranchService branchService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private BranchMapper branchMapper;

    @Autowired
    private NameValidatorService nameValidatorService;

    @Test
    void findById_workingTest(){
        //given
        String name = "SECOND";

        Branch build1 = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName("FIRST").build();
        branchRepository.save(build1);

        Branch build2 = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName(name).build();
        branchRepository.save(build2);

        //when
        ResponseBranchDTO serviceResponse = branchService.findById(build2.getId());

        //then
        assertEquals(name, serviceResponse.name());
        assertTrue(branchRepository.existsById(build2.getId()));
    }

    @Test
    void findById_entityDoesNotExistThrowsException(){
        //given
        long notExistingEntityId = 1234L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> branchService.findById(notExistingEntityId));

        //then
        assertEquals("Branch with id " + notExistingEntityId + " does not exist", exception.getMessage());
    }

    @Test
    void findAll_workingTest(){
        //given
        Branch branch1 = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName("FIRST").build();
        Branch branch2 = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName("SECOND").build();
        Branch branch3 = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName("THIRD").build();
        branchRepository.saveAll(List.of(branch1,branch2,branch3));

        ResponseBranchDTO responseBranchDTO1 = branchMapper.toResponseBranchDTO(branch1);
        ResponseBranchDTO responseBranchDTO2 = branchMapper.toResponseBranchDTO(branch2);
        ResponseBranchDTO responseBranchDTO3 = branchMapper.toResponseBranchDTO(branch3);

        //when
        List<ResponseBranchDTO> serviceResponse = branchService.findAll();

        //then
        assertEquals(3, serviceResponse.size());
        assertTrue(serviceResponse.containsAll(List.of(responseBranchDTO1,responseBranchDTO2, responseBranchDTO3)));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponseBranchDTO> serviceResponse = branchService.findAll();

        //then
        assertEquals(0,serviceResponse.size());
        assertTrue(serviceResponse.isEmpty());
    }

    @Test
    void createBranch_workingTest(){
        //given
        String name = "BEST";
        long regionId = defaultSavedRegion().getId();

        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().withName(name).withRegionId(regionId).build();

        //when
        ResponseBranchDTO serviceResponse = branchService.createBranch(createBranchDTO);
        System.out.println(serviceResponse);

        //then
        assertTrue(branchRepository.existsByName(serviceResponse.name()));

        assertEquals(name,serviceResponse.name());
        assertEquals(regionId,serviceResponse.regionId());
    }

    @Test
    void createBranch_entityWithThisNameAlreadyExistThrowsException(){
        //given
        String previousName = "OLD";
        Branch previousBranch = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName(previousName).build();
        branchRepository.save(previousBranch);

        CreateBranchDTO createBranchDTO = new TestCreateBranchDTO().withName(previousName).build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> branchService.createBranch(createBranchDTO));

        //then
        assertEquals("Branch with name " + previousName + " already exist", exception.getMessage());
        assertTrue(branchRepository.existsByName(previousName));
    }

    @Test
    void createBranch_nameValidatorTest(){
        //given
        String name = "  WeIrD        namE     ";
        Region region = defaultSavedRegion();

        CreateBranchDTO createBranchDTO =
                new TestCreateBranchDTO().withName(name).withRegionId(region.getId()).build();

        String expectedSavedName = "WEIRDNAME";

        //when
        ResponseBranchDTO serviceResponse = branchService.createBranch(createBranchDTO);

        //then
        assertEquals(expectedSavedName, serviceResponse.name());
        assertTrue(branchRepository.existsByName(serviceResponse.name()));
    }

    @Test
    void createBranch_invalidNameThrowsException(){
        //given
        String name = "  !  @     # $   %      ^  &     *  ()  {} ";
        Region region = defaultSavedRegion();

        CreateBranchDTO createBranchDTO =
                new TestCreateBranchDTO().withName(name).withRegionId(region.getId()).build();

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> branchService.createBranch(createBranchDTO));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void updateBranch_workingTest(){
        //given
        String originalName = "ORIGINAL";
        Branch branch = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName(originalName).build();
        branchRepository.save(branch);

        String newName = "NEW";
        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().withName(newName).build();

        //when
        ResponseBranchDTO serviceResponse = branchService.updateBranch(branch.getId(), updateBranchDTO);

        //then
        assertEquals(newName, serviceResponse.name());
        assertEquals(branch.getId(),serviceResponse.id());
    }

    @Test
    void updateBranch_entityDoesNotExistThrowsException(){
        //given
        long randomId = 123123L;
        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> branchService.updateBranch(randomId, updateBranchDTO));

        //then
        assertEquals("Branch with id " + randomId + " does not exist", exception.getMessage());
        assertFalse(branchRepository.existsById(randomId));
    }

    @Test
    void updateBranch_idIsNullThrowsException(){
        //given
        Long nullId = null;
        UpdateBranchDTO updateBranchDTO = new TestUpdateBranchDTO().build();

        //when
        assertThrows(ConstraintViolationException.class, () -> branchService.updateBranch(nullId, updateBranchDTO));

        //then
    }

    @Test
    void delete_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().withRegion(defaultSavedRegion()).build();
        branchRepository.save(branch);

        long id = branch.getId();

        //when
        branchService.delete(id);

        //then
        assertFalse(branchRepository.existsById(id));
    }

    @Test
    void delete_entityDoesNotExistThrowsException(){
        //given
        long randomId = 12345L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> branchService.delete(randomId));

        //then
        assertEquals("Branch with id " + randomId + " does not exist", exception.getMessage());
    }

    @Test
    void existsById_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().withRegion(defaultSavedRegion()).build();
        branchRepository.save(branch);

        long id = branch.getId();

        //when
        boolean response = branchService.exists(id);

        //then
        assertTrue(response);
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "NAME";
        Branch branch = new TestBranchBuilder().withRegion(defaultSavedRegion()).withName(name).build();
        branchRepository.save(branch);

        //when
        boolean response = branchService.exists(name);

        //then
        assertTrue(response);
    }

    private Region defaultSavedRegion(){
        Region region = new TestRegionBuilder().build();
        return regionService.save(region);
    }
}
