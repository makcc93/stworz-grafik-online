package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.branch.*;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StoreServiceImplIT {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreEntityService storeEntityService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private BranchService branchService;

    @Autowired
    private StoreBuilder storeBuilder;

    @Autowired
    private RegionService regionService;

    private Region region;
    private Branch branch;

    @BeforeEach
    void setupRegionAndBranch(){
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);
    }

    @Test
    void findAll_workingTest(){
        //given
        Store firstStore = new TestStoreBuilder().withBranch(branch).build();
        Store secondStore = new TestStoreBuilder().withName("SECONDNAME").withStoreCode("SN").withBranch(branch).build();
        Store thirdStore = new TestStoreBuilder().withName("THIRDNAME").withStoreCode("TN").withBranch(branch).build();

        storeRepository.saveAll(List.of(firstStore,secondStore,thirdStore));

        //when
        List<ResponseStoreDTO> responseStoresDTOS = storeRepository.findAll().stream()
                .map(store -> storeMapper.toResponseStoreDto(store))
                .toList();

        //then
        assertEquals(3,responseStoresDTOS.size());
        assertTrue(responseStoresDTOS.contains(storeMapper.toResponseStoreDto(firstStore)));
        assertTrue(responseStoresDTOS.contains(storeMapper.toResponseStoreDto(secondStore)));
        assertTrue(responseStoresDTOS.contains(storeMapper.toResponseStoreDto(thirdStore)));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponseStoreDTO> dtos = storeService.findAll();

        //then
        assertEquals(0,dtos.size());
        assertDoesNotThrow(() -> storeService.findAll());
    }

    @Test
    void findById_workingTest(){
        //given
        Store firstStore = new TestStoreBuilder().withBranch(branch).build();
        Store secondStore = new TestStoreBuilder().withName("SECONDNAME").withStoreCode("SN").withBranch(branch).build();
        Store thirdStore = new TestStoreBuilder().withName("THIRDNAME").withStoreCode("TN").withBranch(branch).build();

        storeRepository.saveAll(List.of(firstStore,secondStore,thirdStore));

        //when
        ResponseStoreDTO responseFirstStore = storeService.findById(firstStore.getId());

        //then
        assertEquals(firstStore.getName(),responseFirstStore.name());
        assertEquals(firstStore.getId(),responseFirstStore.id());
        assertEquals(firstStore.getCreatedAt(),responseFirstStore.createdAt());
        assertEquals(firstStore.getBranch().getId(),responseFirstStore.branchId());
    }

    @Test
    void findById_unknownEntityThrowsException(){
        //given
        Long unknownId = 123L;
        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> storeService.findById(unknownId));

        //then
        assertEquals("Cannot find store by id " + unknownId,exception.getMessage());
    }

    @Test
    void findById_idIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.findById(null));

        //then
    }

    @Test
    void create_Store_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withBranch(branch).build();
        //when
        ResponseStoreDTO responseStoreDTO = storeService.createStore(createStoreDTO);

        //then
        assertTrue(storeRepository.existsById(responseStoreDTO.id()));

        assertEquals(createStoreDTO.name(),responseStoreDTO.name());
        assertEquals(createStoreDTO.storeCode(),responseStoreDTO.storeCode());
    }

    @Test
    void createStore_storeWithThisNameAlreadyExistThrowsException(){
        //given
        String theSameName = "TESTINGNAME";
        Long theSameBranchId = branch.getId();

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                theSameName,
                "00",
                "TESTINGLOCATION",
                theSameBranchId
        );

        storeService.createStore(createStoreDTO);

        CreateStoreDTO sameNameDTO = new CreateStoreDTO(
                theSameName,
                "11",
                "LOCATION",
                theSameBranchId
        );

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> storeService.createStore(sameNameDTO));

        //then
        assertEquals("Store with name " + theSameName + " already exists", exception.getMessage());
    }

    @Test
    void createStore_storeWithThisStoreCodeAlreadyExistsThrowsException(){
        //given
        String theSameStoreCode = "00";
        Long theSameBranchId = branch.getId();

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "OLDNAME",
                theSameStoreCode,
                "TESTINGLOCATION",
                theSameBranchId
        );

        storeService.createStore(createStoreDTO);

        CreateStoreDTO sameStoreCodeDTO = new CreateStoreDTO(
                "NEWNAME",
                theSameStoreCode,
                "LOCATION",
                theSameBranchId
        );

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> storeService.createStore(sameStoreCodeDTO));

        //then
        assertEquals("Store with code " + theSameStoreCode + " already exists",exception.getMessage());
    }

    @Test
    void createStore_cannotFindBranchThrowsException(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withBranch(branch).build();
        Long branchId = branch.getId();
        branchService.delete(branchId);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> storeService.createStore(createStoreDTO));

        //then
        assertEquals("Cannot find branch by id " + branchId, exception.getMessage());
    }

    @Test
    void createStore_invalidNameThrowsException(){
        //given
        String illegalName = "!LL3G4L";
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withName(illegalName).withBranch(branch).build();

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> storeService.createStore(createStoreDTO));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void createStore_invalidDTOstoreCodeThrowsException(){
        //given
        String tooLongStoreCode = "abcd";
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withStoreCode(tooLongStoreCode).withBranch(branch).build();

        //when
        assertThrows(ValidationException.class, () -> storeService.createStore(createStoreDTO));
        //then
    }

    @Test
    void update_workingTest(){
        //given
        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO()
                .withBranch(branch)
                .withName("TESTINGNAME")
                .withStoreCode("00")
                .withLocation("TESTINGLOCATION")
                .build();

        ResponseStoreDTO responseStoreDTO = storeService.createStore(createStoreDTO);
        Store store = storeRepository.findById(responseStoreDTO.id()).orElseThrow();

        String newName = "SHOULDBETHISNAME";
        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                newName,
                null,
                null,
                null,
                true,
                null
        );

        //when
        storeService.update(responseStoreDTO.id(),updateStoreDTO);

        //then
        assertEquals(newName, store.getName());
        assertEquals(createStoreDTO.storeCode(),store.getStoreCode());
        assertEquals(createStoreDTO.branchId(),store.getBranch().getId());
        assertEquals(createStoreDTO.location(),store.getLocation());
    }

    @Test
    void update_notExistingEntityThrowsException(){
        //given
        Long randomId = 2141L;
        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                "NAME",
                null,
                null,
                null,
                true,
                null
        );

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> storeService.update(randomId, updateStoreDTO));

        //then
        assertEquals("Cannot find store by id " + randomId,exception.getMessage());
    }

    @Test
    void updateStore_idIsNullThrowsException() {
        //given
        Long id = null;
        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().build();

        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.update(id, updateStoreDTO));
        //then
    }

    @Test
    void updateStore_dtoIsNullThrowsException(){
        //given
        Long id = 1L;
        UpdateStoreDTO updateStoreDTO = null;

        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.update(id, updateStoreDTO));
        //then
    }

    @Test
    void updateStore_invalidNameInDTOthrowsException(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);
        Long id = store.getId();

        String invalidName = "INV@LID";
        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().withName(invalidName).build();

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> storeService.update(id,updateStoreDTO));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void updateStore_updateWithNotExistingBranchThrowsException(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);
        Long id = store.getId();

        Branch newBranch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(newBranch);
        Long newBranchId = newBranch.getId();

        branchService.delete(newBranchId);

        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().withBranch(newBranch).build();

        //when
        EntityNotFoundException exception
                = assertThrows(EntityNotFoundException.class, () -> storeService.update(id, updateStoreDTO));

        //then
        assertEquals("Cannot find branch by id " + newBranchId, exception.getMessage());
    }

    @Test
    void updateStore_updatedBranchIdDoesNotExistThrowsException(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);
        Long id = store.getId();

        UpdateStoreDTO updateStoreDTO = new TestUpdateStoreDTO().withBranch(branch).build();

        Long branchId = branch.getId();
        branchService.delete(branchId);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> storeService.update(id, updateStoreDTO));

        //then
        assertEquals("Cannot find branch by id " + branchId, exception.getMessage());
    }

    @Test
    void existsById_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);

        //when
        boolean exists = storeService.existsById(store.getId());
        boolean shouldNotExist = storeService.existsById(123456L);

        //then
        assertTrue(exists);

        assertFalse(shouldNotExist);
    }

    @Test
    void existsById_idIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.existsById(null));
        //then
    }

    @Test
    void existsByNameAndStoreCode_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);

        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO(store.getName(), store.getStoreCode());

        StoreNameAndCodeDTO notExisting = new StoreNameAndCodeDTO("RANDOMNAME","AA");

        //when
        boolean exists = storeService.existsByNameAndCode(storeNameAndCodeDTO);

        boolean shouldBeFalse = storeService.existsByNameAndCode(notExisting);

        //then
        assertTrue(exists);

        assertFalse(shouldBeFalse);
    }

    @Test
    void existsByNameAndStoreCode_dtoIsNullThrowsException(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = null;

        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.existsByNameAndCode(storeNameAndCodeDTO));

        //then
    }

    @Test
    void existsByNameAndStoreCode_invalidNameInsideDTOthrowsException(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO("AAA!@", "AA");

        //when
        assertThrows(ValidationException.class, () -> storeService.existsByNameAndCode(storeNameAndCodeDTO));
        //then
    }

    @Test
    void existsByNameAndStoreCode_invalidCodeInsideDTOthrowsException(){
        //given
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO("NAME", "TOOLONG");

        //when
        assertThrows(ValidationException.class, () -> storeService.existsByNameAndCode(storeNameAndCodeDTO));
        //then
    }

    @Test
    void delete_workingTest(){
        //given
        Store storeToStay = new TestStoreBuilder().withName("TOSTAY").withBranch(branch).build();
        storeRepository.save(storeToStay);

        Store storeToDelete = new TestStoreBuilder().withName("TODELETE").withBranch(branch).build();
        storeRepository.save(storeToDelete);

        //when
        storeService.delete(storeToDelete.getId());

        //then
        assertTrue(storeService.existsById(storeToStay.getId()));

        assertFalse(storeService.existsById(storeToDelete.getId()));
    }

    @Test
    void delete_notEntityToDeleteThrowsException(){
        //given
        Long notExistingEntityId = 1234L;

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> storeService.delete(notExistingEntityId));

        //then
        assertEquals("Store with id " + notExistingEntityId +" does not exist",exception.getMessage());
    }

    @Test
    void delete_idIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.delete(null));
        //then
    }

    @Test
    void save_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();

        //when
        ResponseStoreDTO serviceResponse = storeService.save(store);

        //then
        assertEquals(store.getName(), serviceResponse.name());
        assertTrue(storeRepository.existsById(store.getId()));
    }

    @Test
    void save_entityIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> storeService.save(null));
        //then
    }

    @Test
    void saveEntity_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();

        //when
        storeEntityService.saveEntity(store);

        //then
        assertTrue(storeRepository.existsById(store.getId()));
    }

    @Test
    void saveEntity_entityIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> storeEntityService.saveEntity(null));
        //then
    }

    @Test
    void getEntityById_workingTest(){
        //given
        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);
        Long id = store.getId();

        //when
        Store serviceResponse = storeEntityService.getEntityById(id);

        //then
        assertEquals(id, serviceResponse.getId());
        assertEquals(store.getName(), serviceResponse.getName());
        assertEquals(store.getStoreCode(), serviceResponse.getStoreCode());
    }

    @Test
    void getEntityById_cannotFindEntityThrowsException(){
        //given
        Long randomId = 1234L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> storeEntityService.getEntityById(randomId));

        //then
        assertEquals("Cannot find store by id " + randomId, exception.getMessage());
    }

    @Test
    void getEntityById_idIsNullThrowsException(){
        //given
        //when
        assertThrows(ConstraintViolationException.class, () -> storeEntityService.getEntityById(null));
        //then
    }
}
