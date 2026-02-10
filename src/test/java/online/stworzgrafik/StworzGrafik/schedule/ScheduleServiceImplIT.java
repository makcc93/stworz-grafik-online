package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @MockitoBean
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

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
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

    @Test
    void findEntityById_scheduleDoesNotExistThrowException(){
        //given
        Long scheduleId = 1234L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> scheduleService.findEntityById(scheduleId));

        //then
        assertEquals("Cannot find schedule by id " + scheduleId, exception.getMessage());
    }

    @Test
    void findEntityById_loggerUserHasNotAccessToThisStoreThrowsException(){
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).withRegion(region).withBranch(branch).build();
        scheduleRepository.save(schedule);

        Long storeId = store.getId();
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        Long scheduleId = schedule.getId();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.findEntityById(scheduleId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());
    }

    @Test void createSchedule_workingTest(){
        //given
        Integer year = 2011;
        Integer month = 1;
        String name = "JANUARY";
        String scheduleStatusName = "IN_PROGRESS";

        CreateScheduleDTO createScheduleDTO = new TestCreateScheduleDTO().withYear(year).withMonth(month).withName(name).withScheduleStatusName(scheduleStatusName).build();

        System.out.println(createScheduleDTO);
        //when
        ResponseScheduleDTO serviceResponse = scheduleService.createSchedule(store.getId(), createScheduleDTO);

        //then
        assertEquals(year,serviceResponse.year());
        assertEquals(month,serviceResponse.month());
        assertEquals(name,serviceResponse.name());
        assertEquals(scheduleStatusName,serviceResponse.scheduleStatusName());
    }
}