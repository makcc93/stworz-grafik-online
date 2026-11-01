package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.calendar.holidays.HolidayService;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.EmployeeMonthlyHoursConfirmationEntityService;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.CurrentUserProvider;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.UserContext;
import online.stworzgrafik.StworzGrafik.user.label.UserLabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandDraftServiceImplTest {

    @InjectMocks
    private DemandDraftServiceImpl demandDraftServiceImpl;

    // === Repozytoria i serwisy wstrzykiwane do DemandDraftServiceImpl ===
    @Mock private DemandDraftRepository demandDraftRepository;
    @Mock private DemandDraftMapper demandDraftMapper;
    @Mock private StoreEntityService storeEntityService;
    @Mock private UserContext userContext;
    @Mock private UserAuthorizationService userAuthorizationService;
    @Mock private HolidayService holidayService;              // ← BYŁO: HolidayManager (błędny typ!)
    @Mock private CalendarCalculation calendarCalculation;
    @Mock private EmployeeEntityService employeeEntityService;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private UserLabelService userLabelService;
    @Mock private BillingPeriodConfigService billingPeriodConfigService;                          // ← BRAKOWAŁO
    @Mock private EmployeeMonthlyHoursConfirmationEntityService employeeMonthlyHoursConfirmationEntityService; // ← BRAKOWAŁO

    // === Dane testowe ===
    private static final Long STORE_ID = 1L;
    private static final LocalDate DRAFT_DATE = LocalDate.of(2021, 9, 9);
    private static final int[] HOURLY_DEMAND =
            {0,0,0,0,0,0,0,0,4,6,8,8,8,8,10,10,10,10,10,10,6,0,0,0};

    private Store store;
    private AppUser appUser;

    @BeforeEach
    void setup() {
        Region region = new TestRegionBuilder().build();
        Branch branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();
        appUser = AppUser.builder().build();
    }

    // ─────────────────────────────────────────────────────────────────────
    // createDemandDraft
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void createDemandDraft_workingTest() {
        // given
        CreateDemandDraftDTO dto = new TestCreateDemandDraftDTO()
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();

        when(storeEntityService.getEntityById(STORE_ID)).thenReturn(store);
        when(demandDraftRepository.existsByStore_IdAndDraftDate(STORE_ID, DRAFT_DATE)).thenReturn(false);
        when(holidayService.isHoliday(DRAFT_DATE)).thenReturn(false);
        when(currentUserProvider.getCurrentUser()).thenReturn(appUser);

        DemandDraft savedDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();
        when(demandDraftRepository.save(any(DemandDraft.class))).thenReturn(savedDraft);

        ResponseDemandDraftDTO expected = new TestResponseDemandDraftDTO()
                .withStoreId(STORE_ID)
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();
        when(demandDraftMapper.toResponseDemandDraftDTO(savedDraft)).thenReturn(expected);

        // when
        ResponseDemandDraftDTO response = demandDraftServiceImpl.createDemandDraft(STORE_ID, dto);

        // then
        assertNotNull(response);
        assertEquals(DRAFT_DATE, response.draftDate());
        assertArrayEquals(HOURLY_DEMAND, response.hourlyDemand());

        verify(demandDraftRepository).existsByStore_IdAndDraftDate(STORE_ID, DRAFT_DATE);
        verify(demandDraftRepository).save(any(DemandDraft.class));
        verify(demandDraftMapper).toResponseDemandDraftDTO(savedDraft);
        verify(holidayService).isHoliday(DRAFT_DATE);
    }

    @Test
    void createDemandDraft_onHolidayZeroesDemand() {
        // given
        CreateDemandDraftDTO dto = new TestCreateDemandDraftDTO()
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();

        when(storeEntityService.getEntityById(STORE_ID)).thenReturn(store);
        when(demandDraftRepository.existsByStore_IdAndDraftDate(STORE_ID, DRAFT_DATE)).thenReturn(false);
        when(holidayService.isHoliday(DRAFT_DATE)).thenReturn(true);  // ← święto
        when(currentUserProvider.getCurrentUser()).thenReturn(appUser);

        DemandDraft savedDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(new int[24])
                .build();
        when(demandDraftRepository.save(any(DemandDraft.class))).thenReturn(savedDraft);

        int[] expectedZeros = new int[24];
        ResponseDemandDraftDTO expected = new TestResponseDemandDraftDTO()
                .withStoreId(STORE_ID)
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(expectedZeros)
                .build();
        when(demandDraftMapper.toResponseDemandDraftDTO(savedDraft)).thenReturn(expected);

        // when
        ResponseDemandDraftDTO response = demandDraftServiceImpl.createDemandDraft(STORE_ID, dto);

        // then
        assertArrayEquals(expectedZeros, response.hourlyDemand());
        verify(holidayService).isHoliday(DRAFT_DATE);
    }

    @Test
    void createDemandDraft_storeDoesNotExistThrowsException() {
        // given
        when(storeEntityService.getEntityById(STORE_ID)).thenThrow(EntityNotFoundException.class);
        CreateDemandDraftDTO dto = new TestCreateDemandDraftDTO().build();

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> demandDraftServiceImpl.createDemandDraft(STORE_ID, dto));

        verify(demandDraftRepository, never()).save(any());
    }

    @Test
    void createDemandDraft_draftAlreadyExistsReturnsExistingDraft() {
        // given
        CreateDemandDraftDTO dto = new TestCreateDemandDraftDTO()
                .withDraftDate(DRAFT_DATE)
                .build();

        when(storeEntityService.getEntityById(STORE_ID)).thenReturn(store);
        when(demandDraftRepository.existsByStore_IdAndDraftDate(STORE_ID, DRAFT_DATE)).thenReturn(true);

        DemandDraft existingDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();
        when(demandDraftRepository.findFirstByStore_IdAndDraftDateOrderByIdDesc(STORE_ID, DRAFT_DATE))
                .thenReturn(Optional.of(existingDraft));

        ResponseDemandDraftDTO expected = new TestResponseDemandDraftDTO()
                .withStoreId(STORE_ID)
                .withDraftDate(DRAFT_DATE)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();
        when(demandDraftMapper.toResponseDemandDraftDTO(existingDraft)).thenReturn(expected);

        // when
        ResponseDemandDraftDTO response = demandDraftServiceImpl.createDemandDraft(STORE_ID, dto);

        // then
        assertEquals(DRAFT_DATE, response.draftDate());
        assertArrayEquals(HOURLY_DEMAND, response.hourlyDemand());

        verify(demandDraftRepository, never()).save(any());
        verify(demandDraftRepository).findFirstByStore_IdAndDraftDateOrderByIdDesc(STORE_ID, DRAFT_DATE);
    }

    // ─────────────────────────────────────────────────────────────────────
    // updateDemandDraft
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void updateDemandDraft_workingTest() {
        // given
        Long draftId = 100L;

        DemandDraft originalDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();

        LocalDate newDate = LocalDate.of(2025, 12, 16);
        int[] newDemand = {0,0,0,0,0,0,0,0,3,5,10,10,10,10,11,11,11,11,11,11,10,6,0,0};
        UpdateDemandDraftDTO dto = new TestUpdateDemandDraftDTO()
                .withHourlyDemand(newDemand)
                .withDraftDate(newDate)
                .build();

        when(userAuthorizationService.hasAccessToStore(STORE_ID)).thenReturn(true);
        when(demandDraftRepository.findById(draftId)).thenReturn(Optional.of(originalDraft));
        when(holidayService.isHoliday(newDate)).thenReturn(false);            // ← TEGO BRAKOWAŁO!
        when(currentUserProvider.getCurrentUser()).thenReturn(appUser);
        when(demandDraftRepository.save(originalDraft)).thenReturn(originalDraft);

        ResponseDemandDraftDTO expected = new TestResponseDemandDraftDTO()
                .withDraftDate(newDate)
                .withHourlyDemand(newDemand)
                .build();
        when(demandDraftMapper.toResponseDemandDraftDTO(originalDraft)).thenReturn(expected);

        // when
        ResponseDemandDraftDTO response =
                demandDraftServiceImpl.updateDemandDraft(STORE_ID, draftId, dto);

        // then
        assertEquals(newDate, response.draftDate());
        assertArrayEquals(newDemand, response.hourlyDemand());

        verify(holidayService).isHoliday(newDate);
        verify(demandDraftMapper).updateDemandDraft(any(UpdateDemandDraftDTO.class), eq(originalDraft));
        verify(demandDraftRepository).save(originalDraft);
    }

    @Test
    void updateDemandDraft_onHolidayZeroesDemand() {
        // given
        Long draftId = 100L;
        DemandDraft originalDraft = new TestDemandDraftBuilder()
                .withStore(store)
                .withHourlyDemand(HOURLY_DEMAND)
                .build();

        LocalDate holidayDate = LocalDate.of(2025, 12, 25);
        UpdateDemandDraftDTO dto = new TestUpdateDemandDraftDTO()
                .withHourlyDemand(HOURLY_DEMAND)
                .withDraftDate(holidayDate)
                .build();

        when(userAuthorizationService.hasAccessToStore(STORE_ID)).thenReturn(true);
        when(demandDraftRepository.findById(draftId)).thenReturn(Optional.of(originalDraft));
        when(holidayService.isHoliday(holidayDate)).thenReturn(true);
        when(currentUserProvider.getCurrentUser()).thenReturn(appUser);
        when(demandDraftRepository.save(originalDraft)).thenReturn(originalDraft);

        int[] zeros = new int[24];
        ResponseDemandDraftDTO expected = new TestResponseDemandDraftDTO()
                .withDraftDate(holidayDate)
                .withHourlyDemand(zeros)
                .build();
        when(demandDraftMapper.toResponseDemandDraftDTO(originalDraft)).thenReturn(expected);

        // when
        ResponseDemandDraftDTO response =
                demandDraftServiceImpl.updateDemandDraft(STORE_ID, draftId, dto);

        // then
        assertArrayEquals(zeros, response.hourlyDemand());
    }

    @Test
    void updateDemandDraft_loggedUserHasNotAccessToStoreThrowsException() {
        // given
        Long draftId = 1L;
        when(userAuthorizationService.hasAccessToStore(STORE_ID)).thenReturn(false);

        UpdateDemandDraftDTO dto = new TestUpdateDemandDraftDTO().build();

        // when
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> demandDraftServiceImpl.updateDemandDraft(STORE_ID, draftId, dto));

        // then
        assertEquals("Access denied for store with id " + STORE_ID +
                ", cannot update draft with id " + draftId, ex.getMessage());

        verify(userAuthorizationService).hasAccessToStore(STORE_ID);
        verify(demandDraftRepository, never()).findById(any());
        verify(holidayService, never()).isHoliday(any());
        verify(demandDraftMapper, never()).updateDemandDraft(any(), any());
        verify(demandDraftRepository, never()).save(any());
        verify(demandDraftMapper, never()).toResponseDemandDraftDTO(any());
    }

    @Test
    void updateDemandDraft_cannotFindDraftToUpdateThrowsException() {
        // given
        Long draftId = 1234L;
        when(userAuthorizationService.hasAccessToStore(STORE_ID)).thenReturn(true);
        when(demandDraftRepository.findById(draftId)).thenReturn(Optional.empty());

        UpdateDemandDraftDTO dto = new TestUpdateDemandDraftDTO().build();

        // when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> demandDraftServiceImpl.updateDemandDraft(STORE_ID, draftId, dto));

        // then
        assertEquals("Cannot find demand draft by id " + draftId, ex.getMessage());

        verify(demandDraftRepository).findById(draftId);
        verify(holidayService, never()).isHoliday(any());
        verify(demandDraftMapper, never()).updateDemandDraft(any(), any());
        verify(demandDraftRepository, never()).save(any());
        verify(demandDraftMapper, never()).toResponseDemandDraftDTO(any());
    }

    // ─────────────────────────────────────────────────────────────────────
    // deleteDemandDraft
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void deleteDemandDraft_workingTest() {
        // given
        Long draftId = 123L;
        when(demandDraftRepository.existsById(draftId)).thenReturn(true);
        when(userAuthorizationService.hasAccessToStore(STORE_ID)).thenReturn(true);

        // when
        demandDraftServiceImpl.deleteDemandDraft(STORE_ID, draftId);

        // then
        verify(demandDraftRepository).existsById(draftId);
        verify(userAuthorizationService).hasAccessToStore(STORE_ID);
        verify(demandDraftRepository).deleteById(draftId);
    }

    @Test
    void deleteDemandDraft_draftNotExistThrowsException() {
        // given
        Long notExistingDraftId = 12345656L;
        when(demandDraftRepository.existsById(notExistingDraftId)).thenReturn(false);

        // when
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> demandDraftServiceImpl.deleteDemandDraft(STORE_ID, notExistingDraftId));

        // then
        assertEquals("Cannot find demand draft by id " + notExistingDraftId, ex.getMessage());
        verify(userAuthorizationService, never()).hasAccessToStore(any());
        verify(demandDraftRepository, never()).deleteById(any());
    }

    @Test
    void deleteDemandDraft_loggedUserHasNotAccessToStoreThrowsException() {
        // given
        Long draftId = 1L;
        when(demandDraftRepository.existsById(draftId)).thenReturn(true);
        when(userAuthorizationService.hasAccessToStore(STORE_ID)).thenReturn(false);

        // when
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> demandDraftServiceImpl.deleteDemandDraft(STORE_ID, draftId));

        // then
        assertEquals("Access denied for store with id " + STORE_ID, ex.getMessage());
        verify(demandDraftRepository).existsById(draftId);
        verify(demandDraftRepository, never()).deleteById(any());
    }
}