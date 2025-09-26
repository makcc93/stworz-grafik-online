package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StoreServiceImplIT {

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

    @Test
    void findAll_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().build();
        branchRepository.save(branch);

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
        Branch branch = branchBuilder.createBranch("BRANCHFORTEST");
        branchRepository.save(branch);

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
        Branch branch = branchBuilder.createBranch("TESINGBRANCH");
        branchRepository.save(branch);

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "TESTINGLOCATION",
                branch.getId(),
                RegionType.WSCHOD,
                startHour,
                endHour
        );
        //when
        ResponseStoreDTO responseStoreDTO = storeService.create(createStoreDTO);

        //then
        assertTrue(storeRepository.existsById(responseStoreDTO.id()));

        assertEquals(createStoreDTO.name(),responseStoreDTO.name());
        assertEquals(createStoreDTO.storeCode(),responseStoreDTO.storeCode());
        assertEquals(createStoreDTO.region(),responseStoreDTO.region());
        assertEquals(createStoreDTO.openForClientsHour(),responseStoreDTO.openForClientsHour());
        assertEquals(createStoreDTO.closeForClientsHour(),responseStoreDTO.closeForClientsHour());
    }

    @Test
    void create_storeAlreadyExistThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(10,0);
        LocalTime endHour = LocalTime.of(18,0);
        Branch branch = branchBuilder.createBranch("TESINGBRANCH");
        branchRepository.save(branch);

        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "TESTINGLOCATION",
                branch.getId(),
                RegionType.WSCHOD,
                startHour,
                endHour
        );

        storeService.create(createStoreDTO);

        CreateStoreDTO sameNameAndStoreCode = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "LOCATION",
                branch.getId(),
                RegionType.ZACHOD,
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
        Branch branch = branchBuilder.createBranch("TESINGBRANCH");
        branchRepository.save(branch);


        CreateStoreDTO createStoreDTO = new CreateStoreDTO(
                "TESTINGNAME",
                "00",
                "TESTINGLOCATION",
                branch.getId(),
                RegionType.WSCHOD,
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
        Branch branch = new TestBranchBuilder().build();
        branchRepository.save(branch);

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

        //when

        //then
    }
}
