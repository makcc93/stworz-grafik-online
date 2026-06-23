package online.stworzgrafik.StworzGrafik.algorithm;

import de.focus_shift.jollyday.core.HolidayManager;
import online.stworzgrafik.StworzGrafik.TestDatabaseCleaner;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseHoursForEmployeeIndexDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.DaysOffApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.ProposalShiftApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.VacationApplier;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfig;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.CreateEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.EmployeeDelegationService;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffService;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsService;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationService;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleService;
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.TestShiftTypeConfigBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationDTO;
import online.stworzgrafik.StworzGrafik.store.modificationHours.ShiftHourModificationService;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.AppUserService;
import online.stworzgrafik.StworzGrafik.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@Slf4j
@SpringBootTest
class MonthlyStoreScheduleGeneratorIT {

    @Autowired
    private MonthlyStoreScheduleGenerator monthlyStoreScheduleGenerator;

    @Autowired
    private RegionEntityService regionEntityService;

    @Autowired
    private BranchEntityService branchEntityService;

    @Autowired
    private StoreEntityService storeEntityService;

    @Autowired
    private ScheduleEntityService scheduleEntityService;

    @Autowired
    private EmployeeEntityService employeeEntityService;

    @Autowired
    private HolidayManager holidayManager;

    @Autowired
    private ShiftEntityService shiftEntityService;

    @Autowired
    private ShiftTypeConfigService shiftTypeConfigService;

    @Autowired
    private VacationApplier vacationApplier;

    @Autowired
    private EmployeeProposalShiftsService employeeProposalShiftsService;

    @Autowired
    private EmployeeDelegationService employeeDelegationService;

    @Autowired
    private DaysOffApplier daysOffApplier;

    @Autowired
    private EmployeeVacationService employeeVacationService;

    @Autowired
    private EmployeeProposalDaysOffService employeeProposalDaysOffService;

    @Autowired
    private ProposalShiftApplier proposalShiftApplier;

    @Autowired
    private WarehousemanScheduleGenerator warehousemanScheduleGenerator;

    @Autowired
    private DailyShiftGeneratorAlgorithm dailyShiftGeneratorAlgorithm;

    @Autowired
    private EmployeeToShiftMatcher employeeToShiftMatcher;

    @Autowired
    private PositionEntityService positionEntityService;

    @Autowired
    private StoreDeliveryEntityService storeDeliveryEntityService;

    @Autowired
    private DemandDraftService demandDraftService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DemandDraftEntityService demandDraftEntityService;

    @Autowired
    private StoreDeliveryService storeDeliveryService;

    @Autowired
    private ExcelExport excelExport;

    @Autowired
    private ShiftHourModificationService shiftHourModificationService;

    @Autowired
    private BillingPeriodConfigService billingPeriodConfigService;

    @Autowired
    private AppUserService appUserService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    @Autowired
    private TestDatabaseCleaner cleaner;

    // Czerwiec 2026 — domyślny miesiąc dla większości testów
    private final int year = 2026;
    private final int month = 6;

    Employee damMro;
    Employee monBar;
    Employee matKru;
    Employee filKam;
    Employee marNow;
    Employee wojPie;
    Employee micWoc;
    Employee tomZaj;
    Employee agaWar;
    Employee micKoz;
    Employee marPrz;
    Employee marWoj;
    Employee olgDar;
    Employee karNak;
    Employee emiMia;
    Employee pokPok;

    List<Employee> employees;

    private final int[] dayOffProposalSecondDayOfMonth = {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private int[] dayOffProposalSecondAndThirdDayOfMonth = {0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    private int[] proposalShiftEightToFifteen   = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0};
    private final int[] proposalShiftEightToFourteen  = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};
    private final int[] proposalShiftEightToThirteen  = {0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0};
    private int[] proposalShiftEightToTwelwe    = {0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0};
    private int[] proposalShiftFourteenToTwenty = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0};

    private int[] firstTwoWeeks  = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private int[] secondTwoWeeks = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

    private int[] mondayDraft          = {0,0,0,0,0,0,0,0,2,6,7,7,7,7,8,8,8,8,8,5,0,0,0,0};
    private int[] tuesdayDraft         = {0,0,0,0,0,0,0,0,2,5,6,6,6,6,7,7,7,7,6,5,0,0,0,0};
    private int[] wednesdayDraft       = {0,0,0,0,0,0,0,0,2,5,6,6,6,6,6,6,6,6,6,4,0,0,0,0};
    private int[] thursdayDraft        = {0,0,0,0,0,0,0,0,2,5,6,6,6,6,6,6,6,6,6,4,0,0,0,0};
    private int[] fridayDraft          = {0,0,0,0,0,0,0,0,2,5,6,7,7,7,7,7,7,7,7,5,0,0,0,0};
    private int[] saturdayDraft        = {0,0,0,0,0,0,0,0,2,7,8,8,8,8,8,8,8,8,7,5,0,0,0,0};
    private int[] sundayDraft          = {0,0,0,0,0,0,0,0,0,0,6,7,7,7,7,7,7,7,4,0,0,0,0,0};

    private int[] fridayDraftPlusOne        = {0,0,0,0,0,0,0,0,2,6,7,8,8,8,8,8,8,8,8,5,0,0,0,0};
    private int[] mondayDraftPlusOneAllDay  = {0,0,0,0,0,0,0,0,3,7,8,8,8,9,9,9,9,9,8,6,0,0,0,0};

    private int[] mondayDraft_may          = {0,0,0,0,0,0,0,0,2,6,7,7,7,7,8,8,8,8,8,5,0,0,0,0};
    private int[] tuesdayDraft_may         = {0,0,0,0,0,0,0,0,2,5,6,6,6,6,7,7,7,7,6,5,0,0,0,0};
    private int[] wednesdayDraft_may       = {0,0,0,0,0,0,0,0,2,5,6,6,6,6,6,6,6,6,6,4,0,0,0,0};
    private int[] thursdayDraft_may        = {0,0,0,0,0,0,0,0,2,5,6,6,6,6,6,6,6,6,6,4,0,0,0,0};
    private int[] fridayDraft_may          = {0,0,0,0,0,0,0,0,2,5,6,7,7,7,7,7,7,7,7,5,0,0,0,0};
    private int[] saturdayDraft_may        = {0,0,0,0,0,0,0,0,2,7,8,8,8,8,8,8,8,8,7,5,0,0,0,0};

    private Region region;
    private Branch branch;
    private Store store;
    private Long storeId;
    private Schedule schedule;
    private AppUser currentUser;
    private Position position;

    private ShiftTypeConfig vacationTypeConfig  = new TestShiftTypeConfigBuilder().withCode(ShiftCode.VACATION).build();
    private ShiftTypeConfig dayOffTypeConfig    = new TestShiftTypeConfigBuilder().withCode(ShiftCode.DAY_OFF).build();
    private ShiftTypeConfig proposalTypeConfig  = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK_BY_PROPOSAL).build();
    private ShiftTypeConfig standardTypeConfig  = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK).build();
    private ShiftTypeConfig delegationTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.DELEGATION).build();

    @AfterEach
    void clean() {
        cleaner.cleanAll();
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setup() {
        cleaner.cleanAll();

        billingPeriodConfigService.saveAll(List.of(
                BillingPeriodConfig.builder().startMonth(3).durationMonths(3).build(),
                BillingPeriodConfig.builder().startMonth(6).durationMonths(3).build(),
                BillingPeriodConfig.builder().startMonth(9).durationMonths(3).build(),
                BillingPeriodConfig.builder().startMonth(12).durationMonths(3).build()
        ));

        position = positionEntityService.saveEntity(new TestPositionBuilder().withName("TESTOWAPOZYCJA").build());

        region = new TestRegionBuilder().build();
        regionEntityService.saveEntity(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchEntityService.saveEntity(branch);

        store = storeEntityService.createEntityStore(new CreateStoreDTO("NAME", "00", "WARSAW", branch.getId()));
        storeId = store.getId();

        currentUser = appUserService.save(AppUser.builder()
                .login("test-generator-user")
                .password("test")
                .role(UserRole.ADMIN)
                .build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())
        );

        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(true);
        when(userAuthorizationService.getUserStoreId()).thenReturn(store.getId());

        // ShiftTypeConfigs muszą być zapisane przed generateAllShifts i contextFactory
        shiftTypeConfigService.save(vacationTypeConfig);
        shiftTypeConfigService.save(dayOffTypeConfig);
        shiftTypeConfigService.save(proposalTypeConfig);
        shiftTypeConfigService.save(standardTypeConfig);
        shiftTypeConfigService.save(delegationTypeConfig);

        // Shifts muszą być w bazie zanim contextFactory ich szuka
        generateAllShifts();

        // Pracownicy najpierw — potem storeDelivery, bo walidacja ID
        employees = getEmployees();

        // Warehouseman ma już ID po saveAll — bezpieczne
        Employee warehouseman = employees.stream()
                .filter(Employee::isWarehouseman)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Brak pracownika-magazyniera w liście"));
        storeDeliveryService.update(storeId, new UpdateStoreDeliveryDTO(true, warehouseman.getId(), null, null));

        // Drafty dla domyślnego miesiąca (czerwiec 2026)
        setupDraftsForMonth(year, month, mondayDraft, tuesdayDraft, wednesdayDraft, thursdayDraft, fridayDraft, saturdayDraft);

        schedule = new TestScheduleBuilder()
                .withStore(store)
                .withYear(year)
                .withMonth(month)
                .build();
        scheduleEntityService.saveEntity(schedule);
    }

    // Pomocnicza metoda — tworzy drafty dla podanego miesiąca w bazie
    private void setupDraftsForMonth(int y, int m,
                                     int[] monDraft, int[] tueDraft, int[] wedDraft,
                                     int[] thuDraft, int[] friDraft, int[] satDraft) {
        YearMonth yearMonth = YearMonth.of(y, m);
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(y, m, day);

            if (holidayManager.isHoliday(date)) {
                demandDraftService.createDemandDraft(storeId, new CreateDemandDraftDTO(date, new int[24]));
                continue;
            }

            int[] draft = switch (date.getDayOfWeek()) {
                case MONDAY    -> monDraft;
                case TUESDAY   -> tueDraft;
                case WEDNESDAY -> wedDraft;
                case THURSDAY  -> thuDraft;
                case FRIDAY    -> friDraft;
                case SATURDAY  -> satDraft;
                default        -> null; // niedziela — brak draftu
            };

            if (draft != null) {
                demandDraftService.createDemandDraft(storeId, new CreateDemandDraftDTO(date, draft));
            }
        }
    }

    @Test
    void generateMonthlySchedule_realScheduleForMay() throws IOException {
        // Ten test używa maja 2026 — własne drafty, własny schedule
        int mayYear = 2026;
        int mayMonth = 5;

        // Usuń schedule czerwcowy, utwórz majowy
        scheduleService.deleteSchedule(storeId,schedule.getId());
        Schedule maySchedule = new TestScheduleBuilder()
                .withStore(store)
                .withYear(mayYear)
                .withMonth(mayMonth)
                .build();
        scheduleEntityService.saveEntity(maySchedule);

        // Drafty dla maja (nadpisujemy czerwcowe)
        setupDraftsForMonth(mayYear, mayMonth,
                mondayDraft_may, tuesdayDraft_may, wednesdayDraft_may,
                thursdayDraft_may, fridayDraft_may, saturdayDraft_may);

        LocalDate extraDate = LocalDate.of(mayYear, mayMonth, 4);
        List<DemandDraft> extraDateDraft = demandDraftEntityService
                .findAllByStoreIdAndDateBetween(storeId, extraDate, extraDate);
        demandDraftService.updateDemandDraft(storeId, extraDateDraft.getFirst().getId(),
                new UpdateDemandDraftDTO(extraDate, mondayDraftPlusOneAllDay));

        // VACATION
        generateVacationsForMonth(mayYear, mayMonth, micKoz, 18, 26, null);
        generateVacationsForMonth(mayYear, mayMonth, marNow, 14, 15, null);
        generateVacationsForMonth(mayYear, mayMonth, wojPie, 6, 14, null);
        generateVacationsForMonth(mayYear, mayMonth, marPrz, 11, 16, null);
        generateVacationsForMonth(mayYear, mayMonth, agaWar, 1, 8, null);
        generateVacationsForMonth(mayYear, mayMonth, damMro, 1, 10, List.of(12, 13));
        generateVacationsForMonth(mayYear, mayMonth, marWoj, 1, 6, List.of(7));
        generateVacationsForMonth(mayYear, mayMonth, emiMia, 25, 29, null);
        generateVacationsForMonth(mayYear, mayMonth, olgDar, 18, 21, List.of(5, 6, 14));
        generateVacationsForMonth(mayYear, mayMonth, matKru, 14, 14, null);
        generateVacationsForMonth(mayYear, mayMonth, filKam, 21, 21, null);

        // DAYSOFF_PROPOSAL
        generateDayOffProposalsForMonth(mayYear, mayMonth, agaWar, List.of(2));
        generateDayOffProposalsForMonth(mayYear, mayMonth, monBar, List.of(2, 7, 15));
        generateDayOffProposalsForMonth(mayYear, mayMonth, micWoc, List.of(9));
        generateDayOffProposalsForMonth(mayYear, mayMonth, marNow, List.of(16, 5, 26));
        generateDayOffProposalsForMonth(mayYear, mayMonth, marWoj, List.of(13, 16));
        generateDayOffProposalsForMonth(mayYear, mayMonth, karNak, List.of(6, 18, 22, 23));
        generateDayOffProposalsForMonth(mayYear, mayMonth, matKru, List.of(4, 12, 16, 30));
        generateDayOffProposalsForMonth(mayYear, mayMonth, damMro, List.of(11, 22, 25, 26));
        generateDayOffProposalsForMonth(mayYear, mayMonth, olgDar, List.of(23));

        // SHIFT_PROPOSAL
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 14, 8, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 15, 13, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 16, 8, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 18, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 19, 14, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 20, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 21, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 23, 8, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 27, 8, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 28, 8, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 29, 14, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, damMro, 30, 8, 20);

        newGenerateShiftProposalForMonth(mayYear, mayMonth, matKru, 15, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, matKru, 5, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, matKru, 2, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, matKru, 21, 8, 20);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, matKru, 26, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, matKru, 8, 14, 20);

        newGenerateShiftProposalForMonth(mayYear, mayMonth, wojPie, 5, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, monBar, 8, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, monBar, 9, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, agaWar, 14, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, agaWar, 15, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, wojPie, 18, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, monBar, 18, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, wojPie, 19, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, monBar, 19, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, agaWar, 20, 8, 15);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, wojPie, 21, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, wojPie, 26, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, wojPie, 27, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, monBar, 28, 8, 14);
        newGenerateShiftProposalForMonth(mayYear, mayMonth, monBar, 29, 8, 14);

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, mayYear, mayMonth);
    }

    @Test
    void generateMonthlySchedule_workingOn() throws IOException {
        newGenerateShiftProposal(wojPie, 1, 10, 18);
        newGenerateShiftProposal(wojPie, 2, 10, 18);
        newGenerateShiftProposal(wojPie, 3, 10, 18);
        newGenerateShiftProposal(wojPie, 4, 10, 18);
        newGenerateShiftProposal(wojPie, 5, 10, 18);

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, year, month);
    }

    @Test
    void generateMonthlySchedule_warehousemanOnVacationOtherEmployeeTakesHisShifts() throws IOException {
        generateVacation(damMro, firstTwoWeeks);
        generateVacation(micWoc, firstTwoWeeks);
        generateVacation(filKam, firstTwoWeeks);
        generateVacation(marNow, secondTwoWeeks);
        generateVacation(emiMia, secondTwoWeeks);
        generateVacation(karNak, secondTwoWeeks);

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, year, month);
    }

    @Test
    void generateMonthlySchedule_weirdEmployeeHasProposalMorningShiftsAndTwoEmployeesOnVacationInSameTime() throws IOException {
        generateVacation(damMro, firstTwoWeeks);
        generateVacation(micKoz, firstTwoWeeks);
        generateVacation(micWoc, secondTwoWeeks);
        generateVacation(emiMia, secondTwoWeeks);

        LocalDate secMar = LocalDate.of(year, month, 2);
        LocalDate thiMar = LocalDate.of(year, month, 3);
        LocalDate fouMar = LocalDate.of(year, month, 4);
        LocalDate fifMar = LocalDate.of(year, month, 5);
        LocalDate sixMar = LocalDate.of(year, month, 6);

        generateShiftProposal(wojPie, secMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, thiMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, fouMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, fifMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, sixMar, proposalShiftEightToThirteen);

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, year, month);
    }

    @Test
    void generateMonthlySchedule_weirdFifthEmployeeHasProposalMorningShiftsFromOpenAndTwoEmployeesOnVacation() throws IOException {
        generateVacation(damMro, firstTwoWeeks);
        generateVacation(filKam, firstTwoWeeks);
        generateVacation(marNow, secondTwoWeeks);
        generateVacation(emiMia, secondTwoWeeks);

        LocalDate secMar = LocalDate.of(year, month, 2);
        LocalDate thiMar = LocalDate.of(year, month, 3);
        LocalDate fouMar = LocalDate.of(year, month, 4);
        LocalDate fifMar = LocalDate.of(year, month, 5);
        LocalDate sixMar = LocalDate.of(year, month, 6);

        generateShiftProposal(wojPie, secMar, proposalShiftEightToFourteen);
        generateShiftProposal(micKoz, secMar, proposalShiftEightToFourteen);
        generateShiftProposal(agaWar, secMar, proposalShiftEightToFourteen);
        generateShiftProposal(olgDar, secMar, proposalShiftEightToFourteen);
        generateShiftProposal(tomZaj, secMar, proposalShiftEightToFourteen);
        generateShiftProposal(marPrz, secMar, proposalShiftEightToFourteen);

        generateShiftProposal(wojPie, thiMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, fouMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, fifMar, proposalShiftEightToThirteen);
        generateShiftProposal(wojPie, sixMar, proposalShiftEightToThirteen);

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, year, month);
    }

    @Test
    void generateMonthlySchedule_weirdFifthEmployeeHasProposalAfternoonShiftsFromOpenAndTwoEmployeesOnVacationInSameTime() throws IOException {
        generateVacation(damMro, firstTwoWeeks);
        generateVacation(filKam, firstTwoWeeks);
        generateVacation(marNow, secondTwoWeeks);
        generateVacation(emiMia, secondTwoWeeks);

        LocalDate secMar = LocalDate.of(year, month, 2);

        generateShiftProposal(wojPie, secMar, proposalShiftFourteenToTwenty);
        generateShiftProposal(micKoz, secMar, proposalShiftFourteenToTwenty);
        generateShiftProposal(agaWar, secMar, proposalShiftFourteenToTwenty);
        generateShiftProposal(olgDar, secMar, proposalShiftFourteenToTwenty);
        generateShiftProposal(tomZaj, secMar, proposalShiftFourteenToTwenty);
        generateShiftProposal(marPrz, secMar, proposalShiftFourteenToTwenty);

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, year, month);
    }

    @Test
    void generateMonthlySchedule_allEmployeesHasMorningProposals() throws IOException {
        LocalDate secMar = LocalDate.of(year, month, 2);
        for (Employee employee : employees) {
            generateShiftProposal(employee, secMar, proposalShiftEightToFourteen);
        }

        monthlyStoreScheduleGenerator.generateMonthlySchedule(storeId, year, month);
    }

    // ==================== HELPERY ====================

    private List<Employee> getEmployees() {
        damMro = new TestEmployeeBuilder().withFirstName("Damian").withLastName("Mrozicki").withSap(10000001L).withCanOpenCloseStore(true).withStore(store).withPosition(position).build();
        monBar = new TestEmployeeBuilder().withFirstName("Monika").withLastName("Baran").withSap(10000002L).withCanOpenCloseStore(true).withStore(store).withPosition(position).build();
        matKru = new TestEmployeeBuilder().withFirstName("Mateusz").withLastName("Kruk").withSap(10000003L).withCanOpenCloseStore(true).withStore(store).withPosition(position).build();
        filKam = new TestEmployeeBuilder().withFirstName("Filip").withLastName("Kamiński").withSap(10000004L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        marNow = new TestEmployeeBuilder().withFirstName("Martyna").withLastName("Nowicka").withSap(10000005L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        wojPie = new TestEmployeeBuilder().withFirstName("Wojciech").withLastName("Pietruszka").withSap(10000006L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        micWoc = new TestEmployeeBuilder().withFirstName("Michał").withLastName("Woch").withSap(10000007L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        tomZaj = new TestEmployeeBuilder().withFirstName("Tomasz").withLastName("Zając").withSap(10000008L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        agaWar = new TestEmployeeBuilder().withFirstName("Agata").withLastName("Warmińska").withSap(10000009L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        micKoz = new TestEmployeeBuilder().withFirstName("Michał").withLastName("Kozik").withSap(10000010L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        marPrz = new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Przepiórka").withSap(10000011L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        marWoj = new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Wojtas").withSap(10000012L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).withPosition(position).build();
        olgDar = new TestEmployeeBuilder().withFirstName("Olga").withLastName("Darewicz").withSap(10000013L).withStore(store).withPosition(position).build();
        karNak = new TestEmployeeBuilder().withFirstName("Karolina").withLastName("Nakonieczna").withSap(10000014L).withCashier(true).withCanOperateCheckout(true).withStore(store).withPosition(position).build();
        emiMia = new TestEmployeeBuilder().withFirstName("Emil").withLastName("Miazek").withSap(10000015L).withWarehouseman(true).withStore(store).withPosition(position).build();
        pokPok = new TestEmployeeBuilder().withFirstName("Pok").withLastName("Pok").withSap(10000016L).withPok(true).withStore(store).withPosition(position).build();

        List<Employee> saved = employeeEntityService.saveAll(List.of(
                damMro, monBar, matKru, filKam, marNow, wojPie,
                micWoc, tomZaj, agaWar, micKoz, marPrz, marWoj,
                olgDar, karNak, emiMia, pokPok
        ));

        // Odświeżamy referencje polami — saveAll zwraca w tej samej kolejności
        damMro = saved.get(0);  monBar = saved.get(1);
        matKru = saved.get(2);  filKam = saved.get(3);
        marNow = saved.get(4);  wojPie = saved.get(5);
        micWoc = saved.get(6);  tomZaj = saved.get(7);
        agaWar = saved.get(8);  micKoz = saved.get(9);
        marPrz = saved.get(10); marWoj = saved.get(11);
        olgDar = saved.get(12); karNak = saved.get(13);
        emiMia = saved.get(14); pokPok = saved.get(15);

        return saved;
    }

    private void generateAllShifts() {
        List<Shift> shifts = new ArrayList<>();
        int[] minutes = {0, 15, 30, 45};

        for (int startHour = 0; startHour <= 23; startHour++) {
            for (int startMinute : minutes) {
                LocalTime start = LocalTime.of(startHour, startMinute);
                for (int endHour = 0; endHour <= 23; endHour++) {
                    for (int endMinute : minutes) {
                        LocalTime end = LocalTime.of(endHour, endMinute);
                        shifts.add(new TestShiftBuilder().withStartHour(start).withEndHour(end).build());
                    }
                }
            }
        }
        shiftEntityService.saveAll(shifts);
    }

    private void generateShiftProposal(Employee employee, LocalDate date, int[] shiftAsArray) {
        employeeProposalShiftsService.createEmployeeProposalShift(
                storeId, employee.getId(),
                new CreateEmployeeProposalShiftsDTO(date, shiftAsArray));
    }

    private void newGenerateShiftProposal(Employee employee, int dayOfMonth, int startHour, int endHour) {
        newGenerateShiftProposalForMonth(year, month, employee, dayOfMonth, startHour, endHour);
    }

    private void newGenerateShiftProposalForMonth(int y, int m, Employee employee, int dayOfMonth, int startHour, int endHour) {
        LocalDate date = LocalDate.of(y, m, dayOfMonth);
        int[] shiftAsArray = new int[24];
        for (int i = startHour; i < endHour; i++) {
            shiftAsArray[i] = 1;
        }
        employeeProposalShiftsService.createEmployeeProposalShift(
                storeId, employee.getId(),
                new CreateEmployeeProposalShiftsDTO(date, shiftAsArray));
    }

    private void generateDayOffProposals(Employee employee, List<Integer> listOfDays) {
        generateDayOffProposalsForMonth(year, month, employee, listOfDays);
    }

    private void generateDayOffProposalsForMonth(int y, int m, Employee employee, List<Integer> listOfDays) {
        int[] monthlyDayOffProposal = new int[31];
        for (int day : listOfDays) {
            monthlyDayOffProposal[day - 1] = 1;
        }
        employeeProposalDaysOffService.createEmployeeProposalDaysOff(
                storeId, employee.getId(),
                new CreateEmployeeProposalDaysOffDTO(y, m, monthlyDayOffProposal));
    }

    private void generateDelegation(Employee employee, List<Integer> days) {
        int[] delegation = new int[31];
        for (int day : days) {
            delegation[day - 1] = 1;
        }
        employeeDelegationService.createEmployeeProposalDelegation(
                storeId, employee.getId(),
                new CreateEmployeeDelegationDTO(year, month, delegation));
    }

    private void generateVacation(Employee employee, int[] vacationTime) {
        employeeVacationService.createEmployeeProposalVacation(
                storeId, employee.getId(),
                new CreateEmployeeVacationDTO(year, month, vacationTime));
    }

    private void generateVacations(Employee employee, int startDay, int endDay, List<Integer> otherDays) {
        generateVacationsForMonth(year, month, employee, startDay, endDay, otherDays);
    }

    private void generateVacationsForMonth(int y, int m, Employee employee, int startDay, int endDay, List<Integer> otherDays) {
        int[] vacation = new int[31];
        for (int i = startDay; i <= endDay; i++) {
            vacation[i - 1] = 1;
        }
        if (otherDays != null) {
            for (int day : otherDays) {
                vacation[day - 1] = 1;
            }
        }
        employeeVacationService.createEmployeeProposalVacation(
                storeId, employee.getId(),
                new CreateEmployeeVacationDTO(y, m, vacation));
    }
}