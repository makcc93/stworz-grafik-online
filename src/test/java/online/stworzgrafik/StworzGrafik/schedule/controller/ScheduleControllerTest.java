package online.stworzgrafik.StworzGrafik.schedule.controller;

import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleService;
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegionService regionService;

    @Autowired
    private BranchService branchService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private UserAuthorizationService userAuthorizationService;

    @Autowired
    private ScheduleService scheduleService;

    private Schedule schedule;
    private Region region;
    private Branch branch;
    private Store store;
    private Position position;
    private Long storeId;
    private Long positionId;

    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeService.save(store);

        position = new TestPositionBuilder().build();
        positionService.save(position);

        storeId = store.getId();
        positionId = position.getId();

        schedule = new TestScheduleBuilder().withStore(store).build();
        scheduleService.saveSchedule(schedule);

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
    }

    @Test
    void getById_workingTest(){
        //given


        //when&then
        mockMvc.perform(get("/api/stores/" + storeId + "/schedules/" + scheduleId))
    }

}