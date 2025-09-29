package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestCreateStoreDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StoreServiceImplIT {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private StoreBuilder storeBuilder;

    @Autowired
    private BranchBuilder branchBuilder;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void findAll_workingTest(){
        //given
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

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
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

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
        assertEquals(firstStore.getOpenForClientsHour(),responseFirstStore.openForClientsHour());
        assertEquals(firstStore.getCloseForClientsHour(),responseFirstStore.closeForClientsHour());
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
    void create_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(18,0);
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        CreateStoreDTO createStoreDTO = new TestCreateStoreDTO().withBranch(branch).withOpenHour(startHour).withCloseHour(endHour).build();
        //when
        ResponseStoreDTO responseStoreDTO = storeService.create(createStoreDTO);

        //then
        assertTrue(storeRepository.existsById(responseStoreDTO.id()));

        assertEquals(createStoreDTO.name(),responseStoreDTO.name());
        assertEquals(createStoreDTO.storeCode(),responseStoreDTO.storeCode());
        assertEquals(createStoreDTO.openForClientsHour(),responseStoreDTO.openForClientsHour());
        assertEquals(createStoreDTO.closeForClientsHour(),responseStoreDTO.closeForClientsHour());
    }

    @Test
    void create_storeAlreadyExistThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(18,0);
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "TESTINGLOCATION",
                branch.getId(),
                startHour,
                endHour
        );

        storeService.create(createStoreDTO);

        CreateStoreDTO sameNameAndStoreCode = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "LOCATION",
                branch.getId(),
                startHour,
                endHour
        );

        //when
        EntityExistsException entityExistsException = assertThrows(EntityExistsException.class, () -> storeService.create(sameNameAndStoreCode));

        //then
        assertEquals("Store with this name: " + sameNameAndStoreCode.name() + " and store code: " + sameNameAndStoreCode.storeCode() + " already exist",
                entityExistsException.getMessage());
    }

    @Test
    void update_workingTest(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(18,0);
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "TESTINGLOCATION",
                branch.getId(),
                startHour,
                endHour
        );

        ResponseStoreDTO responseStoreDTO = storeService.create(createStoreDTO);
        Store store = storeRepository.findById(responseStoreDTO.id()).orElseThrow();

        String newName = "SHOULDBETHISNAME";
        UpdateStoreDTO updateStoreDTO = new UpdateStoreDTO(
                newName,
                null,
                null,
                null,
                true,
                null,
                null,
                null
        );

        //when
        storeService.update(responseStoreDTO.id(),updateStoreDTO);

        //then
        assertEquals(newName, store.getName());
        assertEquals(createStoreDTO.storeCode(),store.getStoreCode());
        assertEquals(createStoreDTO.branchId(),store.getBranch().getId());
        assertEquals(createStoreDTO.location(),store.getLocation());
        assertEquals(createStoreDTO.openForClientsHour(),store.getOpenForClientsHour());
        assertEquals(createStoreDTO.closeForClientsHour(),store.getCloseForClientsHour());
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
                null,
                null,
                null
        );

        //when
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> storeService.update(randomId, updateStoreDTO));

        //then
        assertEquals("Cannot find store to update by id " + randomId,exception.getMessage());
    }

    @Test
    void existsById_workingTest(){
        //given
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);

        //when
        boolean exists = storeService.exists(store.getId());
        boolean shouldNotExist = storeService.exists(123456L);

        //then
        assertTrue(exists);

        assertFalse(shouldNotExist);
    }

    @Test
    void existsByNameAndStoreCode_workingTest(){
        //given
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        Store store = new TestStoreBuilder().withBranch(branch).build();
        storeRepository.save(store);

        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO(store.getName(), store.getStoreCode());

        StoreNameAndCodeDTO notExisting = new StoreNameAndCodeDTO("RANDOMNAME","AA");

        //when
        boolean exists = storeService.exists(storeNameAndCodeDTO);

        boolean shouldBeFalse = storeService.exists(notExisting);

        //then
        assertTrue(exists);

        assertFalse(shouldBeFalse);
    }

    @Test
    void delete_workingTest(){
        //given
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        Store storeToStay = new TestStoreBuilder().withName("TOSTAY").withBranch(branch).build();
        storeRepository.save(storeToStay);

        Store storeToDelete = new TestStoreBuilder().withName("TODELETE").withBranch(branch).build();
        storeRepository.save(storeToDelete);

        //when
        storeService.delete(storeToDelete.getId());

        //then
        assertTrue(storeService.exists(storeToStay.getId()));

        assertFalse(storeService.exists(storeToDelete.getId()));
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
    void saveEntity_workingTest(){
        //given
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();

        Store store = new TestStoreBuilder().withBranch(branch).build();

        //when
        storeService.saveEntity(store);

        //then
        assertTrue(storeRepository.existsById(store.getId()));
    }

    @Test
    void saveDto_workingTest(){
        //given
        Branch branch = buildAndSaveDefaultBranchWithRegionInside();
        Store store = new TestStoreBuilder().withBranch(branch).build();
        StoreNameAndCodeDTO storeNameAndCodeDTO = new StoreNameAndCodeDTO(store.getName(), store.getStoreCode());

        //when
        storeService.saveDto(storeNameAndCodeDTO);

        //then
        assertTrue(storeRepository.existsByNameAndStoreCode(store.getName(),store.getStoreCode()));
        assertTrue(storeRepository.existsByNameAndStoreCode(storeNameAndCodeDTO.name(),storeNameAndCodeDTO.storeCode()));
    }

    private Branch buildAndSaveDefaultBranchWithRegionInside() {
        Region region = new TestRegionBuilder().build();
        regionRepository.save(region);

        Branch branch = new TestBranchBuilder().withRegion(region).build();
        branchRepository.save(branch);

        return branch;
    }
}
