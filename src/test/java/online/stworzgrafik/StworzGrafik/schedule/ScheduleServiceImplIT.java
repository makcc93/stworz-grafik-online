package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.EntityExistsException;
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
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

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

    @Test
    void createSchedule_scheduleAlreadyExistsThrowsException() {
        //given
        Integer year = 2024;
        Integer month = 12;
        Schedule schedule = new TestScheduleBuilder()
                .withStore(store)
                .withYear(year)
                .withMonth(month)
                .build();
        scheduleRepository.save(schedule);

        CreateScheduleDTO dto = new TestCreateScheduleDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> scheduleService.createSchedule(store.getId(), dto));

        //then
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void createSchedule_accessDeniedThrowsException() {
        //given
        Long storeId = store.getId();
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);
        CreateScheduleDTO dto = new TestCreateScheduleDTO().build();

        //when & then
        assertThrows(AccessDeniedException.class, () -> scheduleService.createSchedule(storeId, dto));
    }

    @Test
    void updateSchedule_workingTest() {
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).withName("Old Name").build();
        scheduleRepository.save(schedule);

        String newName = "Updated Schedule Name";
        UpdateScheduleDTO dto = new TestUpdateScheduleDTO().withName(newName).build();

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.updateSchedule(store.getId(), schedule.getId(), dto);

        //then
        assertEquals(newName, serviceResponse.name());
        Schedule updatedEntity = scheduleRepository.findById(schedule.getId()).get();
        assertEquals(newName, updatedEntity.getName());
    }

    @Test
    void updateSchedule_scheduleNotFoundThrowsException() {
        //given
        Long nonExistentId = 999L;
        UpdateScheduleDTO dto = new TestUpdateScheduleDTO().build();

        //when & then
        assertThrows(EntityNotFoundException.class, () -> scheduleService.updateSchedule(store.getId(), nonExistentId, dto));
    }

    @Test
    void findById_workingTest() {
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).build();
        scheduleRepository.save(schedule);

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.findById(store.getId(), schedule.getId());

        //then
        assertNotNull(serviceResponse);
        assertEquals(schedule.getId(), serviceResponse.id());
    }

    @Test
    void findByCriteria_filterByYearAndMonth_workingTest() {
        //given
        Schedule schedule1 = new TestScheduleBuilder().withStore(store).withYear(2025).withMonth(5).build();
        Schedule schedule2 = new TestScheduleBuilder().withStore(store).withYear(2026).withMonth(6).build();
        scheduleRepository.saveAll(List.of(schedule1, schedule2));

        ScheduleSpecificationDTO criteria = new TestScheduleSpecificationDTO()
                .withYear(2025)
                .withMonth(5)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<ResponseScheduleDTO> result = scheduleService.findByCriteria(store.getId(), criteria, pageable);

        //then
        assertEquals(1, result.getTotalElements());
        assertEquals(2025, result.getContent().getFirst().year());
    }

    @Test
    void findByCriteria_noResults_returnsEmptyPage() {
        //given
        ScheduleSpecificationDTO criteria = new TestScheduleSpecificationDTO().withYear(2001).build();
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<ResponseScheduleDTO> result = scheduleService.findByCriteria(store.getId(), criteria, pageable);

        //then
        assertTrue(result.isEmpty());
    }


    @Test
    void deleteSchedule_workingTest() {
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).build();
        scheduleRepository.save(schedule);
        Long id = schedule.getId();

        //when
        scheduleService.deleteSchedule(store.getId(), id);

        //then
        assertFalse(scheduleRepository.existsById(id));
    }

    @Test
    void deleteSchedule_accessDeniedThrowsException() {
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).build();
        scheduleRepository.save(schedule);

        when(userAuthorizationService.hasAccessToStore(store.getId())).thenReturn(false);

        //when & then
        assertThrows(AccessDeniedException.class, () -> scheduleService.deleteSchedule(store.getId(), schedule.getId()));
    }


    @Test
    void saveSchedule_workingTest() {
        //given
        Schedule schedule = new TestScheduleBuilder().withStore(store).withName("Initial Name").build();

        schedule.setName("Manually Updated Name");

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.saveSchedule(schedule);

        //then
        assertEquals("Manually Updated Name", serviceResponse.name());
        assertNotNull(serviceResponse.id());
    }
}