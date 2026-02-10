package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.PrePersist;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ScheduleServiceImplIT {
    @Autowired
    private ScheduleServiceImpl scheduleService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RegionService regionService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private UserAuthorizationService userAuthorizationService;

    @Autowired
    private StoreService storeService;

    private Region region;
    private Branch branch;
    private Store store;

    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);
    }

    @Test
    void findEntityById_workingTest(){
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).withRegion(region).withBranch(branch).build();
        scheduleRepository.save(schedule);

        Long scheduleId = schedule.getId();

        //when
        Schedule serviceResponse = scheduleService.findEntityById(scheduleId);

        //then
        assertEquals(schedule,serviceResponse);
    }
}