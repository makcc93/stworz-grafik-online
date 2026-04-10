package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseStoreHoursDTO;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.DaysOffApplier;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.ProposalShiftApplier;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.VacationApplier;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
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
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.TestShiftTypeConfigBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.TestStoreDeliveryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    private ShiftEntityService shiftEntityService;

    @Autowired
    private ShiftTypeConfigService shiftTypeConfigService;

    @Autowired
    private VacationApplier vacationApplier;

    @Autowired
    private EmployeeProposalShiftsService employeeProposalShiftsService;

    @Autowired
    private DaysOffApplier daysOffApplier;

    @Autowired
    private EmployeeVacationService employeeVacationService;

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
    private ExcelExport excelExport;

    private StoreDelivery storeDelivery;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    private final int year = 2026;
    private final int month = 3;

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

    private int[] proposalShiftEightToFourteen = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0};
    private int[] proposalShiftEIghtToThirteen = {0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0};

    private int[] firstTwoWeeks = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private int[] secondTwoWeeks = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

    private int[] mondayDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 7, 7, 7, 7, 9, 9, 9, 9, 9, 5, 0, 0, 0, 0};
    private int[] tuesdayDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 7, 7, 7, 7, 8, 8, 8, 8, 8, 4, 0, 0, 0, 0};
    private int[] wednesdayDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 4, 0, 0, 0, 0};
    private int[] thursdayDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 0, 0, 0, 0};
    private int[] fridayDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 7, 7, 8, 8, 9, 9, 9, 9, 9, 6, 0, 0, 0, 0};
    private int[] saturdayDraft = {0, 0, 0, 0, 0, 0, 0, 0, 3, 6, 10, 10, 10, 10, 10, 9, 9, 9, 9, 6, 0, 0, 0, 0};

    private Shift defaultVacationShift = new TestShiftBuilder().withStartHour(LocalTime.of(0, 0)).withEndHour(LocalTime.of(8, 0)).build();
    private Shift defaultDayOffShift = new TestShiftBuilder().withStartHour(LocalTime.of(0, 0)).withEndHour(LocalTime.of(0, 0)).build();

    private ScheduleGeneratorContext context;
    private Region region;
    private Branch branch;
    private Store store;
    private Long storeId;
    private Schedule schedule;
    private Position position = new TestPositionBuilder().withName("TESTOWAPOZYCJA").build();
    private ShiftTypeConfig vacationTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.VACATION).build();
    private ShiftTypeConfig dayOffTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.DAY_OFF).build();
    private ShiftTypeConfig proposalTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK_BY_PROPOSAL).build();
    private ShiftTypeConfig standardTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK).build();


    @BeforeEach
    void setup() {
        position = positionEntityService.saveEntity(position);

        region = new TestRegionBuilder().build();
        regionEntityService.saveEntity(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchEntityService.saveEntity(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeEntityService.saveEntity(store);

        storeId = store.getId();

        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(true);
        when(userAuthorizationService.getUserStoreId()).thenReturn(store.getId());

        schedule = new TestScheduleBuilder().withRegion(region).withBranch(branch).withStore(store).withYear(year).withMonth(month).build();
        scheduleEntityService.saveEntity(schedule);

        List<Employee> employees = getEmployees();

        context = new ScheduleGeneratorContext(
                store.getId(),
                year,
                month,
                schedule,
                store,
                getStoreOpenCloseHour(year,month),
                employees,
                getDraftForEveryDay(year,month),
                getSortedDrafts(year, month),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                getShiftsForEveryDay(year,month),
                new HashMap<>(),
                generateAllShifts(),
                defaultVacationShift,
                defaultDayOffShift,
                shiftTypeConfigService.save(vacationTypeConfig),
                shiftTypeConfigService.save(dayOffTypeConfig),
                shiftTypeConfigService.save(proposalTypeConfig),
                shiftTypeConfigService.save(standardTypeConfig),
                new LinkedHashMap<>(),
                new ArrayList<>(),
                true
        );

        storeDelivery = new TestStoreDeliveryBuilder().withStore(store).withPrimaryEmployee(employees.stream().filter(Employee::isWarehouseman).toList().getFirst()).build();
        storeDeliveryEntityService.save(storeDelivery);

    }

    @Test
    void generateMonthlySchedule_workingOn() throws IOException {
        //given

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }

    @Test
    void generateMonthlySchedule_twoEmployeesHasVacationInSameTime() throws IOException {
        //given
        generateVacation(damMro,firstTwoWeeks);
        generateVacation(filKam,firstTwoWeeks);
        generateVacation(marNow,secondTwoWeeks);
        generateVacation(karNak,secondTwoWeeks);

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }

    @Test
    void generateMonthlySchedule_warehousemanOnVacationOtherEmployeeTakesHisShifts() throws IOException {
        //given
        generateVacation(damMro,firstTwoWeeks);
        generateVacation(filKam,firstTwoWeeks);
        generateVacation(marNow,secondTwoWeeks);
        generateVacation(emiMia,secondTwoWeeks);

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }
    @Test
    void generateMonthlySchedule_employeeHasProposalShifts() throws IOException {
        //given
        LocalDate secMar = LocalDate.of(year,month,2);
        LocalDate thiMar = LocalDate.of(year,month,3);
        LocalDate fouMar = LocalDate.of(year,month,4);
        LocalDate fifMar = LocalDate.of(year,month,5);
        LocalDate sixMar = LocalDate.of(year,month,6);

        generateProposal(wojPie,secMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,thiMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,fouMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,fifMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,sixMar,proposalShiftEightToFourteen);

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }

    @Test
    void generateMonthlySchedule_employeeHasProposalShiftsAndTwoEmployeesOnVacation() throws IOException {
        //given
        generateVacation(damMro,firstTwoWeeks);
        generateVacation(filKam,firstTwoWeeks);
        generateVacation(marNow,secondTwoWeeks);
        generateVacation(emiMia,secondTwoWeeks);

        LocalDate secMar = LocalDate.of(year,month,2);
        LocalDate thiMar = LocalDate.of(year,month,3);
        LocalDate fouMar = LocalDate.of(year,month,4);
        LocalDate fifMar = LocalDate.of(year,month,5);
        LocalDate sixMar = LocalDate.of(year,month,6);

        generateProposal(wojPie,secMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,thiMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,fouMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,fifMar,proposalShiftEightToFourteen);
        generateProposal(wojPie,sixMar,proposalShiftEightToFourteen);

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }

    @Test
    void generateMonthlySchedule_weirdEmployeeHasProposalShiftsAndTwoEmployeesOnVacation() throws IOException {
        //given
        generateVacation(damMro,firstTwoWeeks);
        generateVacation(filKam,firstTwoWeeks);
        generateVacation(marNow,secondTwoWeeks);
        generateVacation(emiMia,secondTwoWeeks);

        LocalDate secMar = LocalDate.of(year,month,2);
        LocalDate thiMar = LocalDate.of(year,month,3);
        LocalDate fouMar = LocalDate.of(year,month,4);
        LocalDate fifMar = LocalDate.of(year,month,5);
        LocalDate sixMar = LocalDate.of(year,month,6);

        generateProposal(wojPie,secMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,thiMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,fouMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,fifMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,sixMar,proposalShiftEIghtToThirteen);

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }

    @Test
    void generateMonthlySchedule_weirdTwoEmployeeHasProposalShiftsAndTwoEmployeesOnVacation() throws IOException {
        //given
        generateVacation(damMro,firstTwoWeeks);
        generateVacation(filKam,firstTwoWeeks);
        generateVacation(marNow,secondTwoWeeks);
        generateVacation(emiMia,secondTwoWeeks);

        LocalDate secMar = LocalDate.of(year,month,2);
        LocalDate thiMar = LocalDate.of(year,month,3);
        LocalDate fouMar = LocalDate.of(year,month,4);
        LocalDate fifMar = LocalDate.of(year,month,5);
        LocalDate sixMar = LocalDate.of(year,month,6);

        generateProposal(wojPie,secMar,proposalShiftEIghtToThirteen);
        generateProposal(micKoz,secMar,proposalShiftEIghtToThirteen);
        generateProposal(agaWar,secMar,proposalShiftEIghtToThirteen);
        generateProposal(olgDar,secMar,proposalShiftEIghtToThirteen);

        generateProposal(wojPie,thiMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,fouMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,fifMar,proposalShiftEIghtToThirteen);
        generateProposal(wojPie,sixMar,proposalShiftEIghtToThirteen);

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }


    private void generateProposal(Employee employee, LocalDate date, int[] shiftAsArray){
        employeeProposalShiftsService.createEmployeeProposalShift(
                storeId,
                employee.getId(),
                new CreateEmployeeProposalShiftsDTO(
                        date,
                        shiftAsArray
                )
        );
    }

    private void generateVacation(Employee employee, int[] vacationTime){
        employeeVacationService.createEmployeeProposalVacation(
                storeId,
                employee.getId(),
                new CreateEmployeeVacationDTO(
                        year,
                        month,
                        vacationTime
                )
        );
    }

    private List<Shift> generateAllShifts() {
        List<Shift> shifts = new ArrayList<>();
        for (int startHour = 0; startHour <= 23; startHour++) {
            LocalTime start = LocalTime.of(startHour, 0);
            for (int endHour = 0; endHour <= 23; endHour++) {
                LocalTime end = LocalTime.of(endHour, 0);
                Shift shift = new TestShiftBuilder()
                        .withStartHour(start)
                        .withEndHour(end)
                        .build();

                shifts.add(shift);
            }
        }

        return shiftEntityService.saveAll(shifts);
    }

    private Map<LocalDate, List<Shift>> getShiftsForEveryDay(Integer year, Integer month){
        Map<LocalDate, List<Shift>> map = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(year,month);
        for (int day = 1; day <= yearMonth.lengthOfMonth();day++){
            LocalDate date = LocalDate.of(year,month,day);

            if (date.getDayOfWeek() == DayOfWeek.MONDAY){
                map.put(date,generateLowestPersonNeededDailyShifts(mondayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.TUESDAY){
                map.put(date,generateLowestPersonNeededDailyShifts(tuesdayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.WEDNESDAY){
                map.put(date,generateLowestPersonNeededDailyShifts(wednesdayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.THURSDAY){
                map.put(date,generateLowestPersonNeededDailyShifts(thursdayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.FRIDAY){
                map.put(date,generateLowestPersonNeededDailyShifts(fridayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY){
                map.put(date,generateLowestPersonNeededDailyShifts(saturdayDraft));
            }
        }

        return map;
    }


    private List<Shift> generateLowestPersonNeededDailyShifts(int[] dailyDemandDraft) {
        List<Shift> startHoursShifts = generateShiftStartHours(dailyDemandDraft);

        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
                .toList();

        return generateShiftEndHours(shiftsSortedDesc, dailyDemandDraft);
    }

    private List<Shift> generateShiftEndHours(List<Shift> shiftsSortedDesc, int[] dailyDemandDraft) {
        int index = 0;
        for (int hourOfDay = 23; hourOfDay >= 0; hourOfDay--) {
            int demand = dailyDemandDraft[hourOfDay];

            if (demand > 0) {
                int nextDemand = (hourOfDay == 23) ? 0 : dailyDemandDraft[hourOfDay + 1];
                for (int i = demand; i > nextDemand; i--) {

                    if (hourOfDay == 23){
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(0,0));
                    } else {
                        shiftsSortedDesc.get(index).setEndHour(LocalTime.of(hourOfDay + 1, 0));
                    }
                    index++;
                }
            }
        }
        return shiftsSortedDesc;
    }

    private List<Shift> generateShiftStartHours(int[] dailyDemandDraft) {
        List<Shift> shifts = new ArrayList<>();

        for (int hourOfDay = 0; hourOfDay < dailyDemandDraft.length; hourOfDay++) {
            int demand = dailyDemandDraft[hourOfDay];

            if (demand != 0) {
                int previousDemand = (hourOfDay == 0) ? 0 : dailyDemandDraft[hourOfDay -1];
                for (int i = demand; i > previousDemand; i--) {
                    Shift shift = new TestShiftBuilder().withStartHour(LocalTime.of(hourOfDay,0)).build();

                    shifts.add(shift);
                }
            }
        }
        return shifts;
    }


    private LinkedHashMap<LocalDate, int[]> getSortedDrafts(Integer year, Integer month){
        return getDraftForEveryDay(year,month).entrySet().stream()
                .sorted(Comparator.comparingInt(
                                (Map.Entry<LocalDate, int[]> entry) -> Arrays.stream(entry.getValue()).sum())
                        .reversed()
                )
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        )
                );
    }

    private Map<LocalDate, int[]> getDraftForEveryDay(Integer year, Integer month){
        Map<LocalDate,int[]> map = new HashMap<>();


        YearMonth yearMonth = YearMonth.of(year,month);
        for (int day = 1; day <= yearMonth.lengthOfMonth();day++){
            LocalDate date = LocalDate.of(year,month,day);

            if (date.getDayOfWeek() == DayOfWeek.MONDAY){
                map.put(date,mondayDraft);
                demandDraftService.createDemandDraft(storeId,new CreateDemandDraftDTO(date,mondayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.TUESDAY){
                map.put(date,tuesdayDraft);
                demandDraftService.createDemandDraft(storeId,new CreateDemandDraftDTO(date,tuesdayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.WEDNESDAY){
                map.put(date,wednesdayDraft);
                demandDraftService.createDemandDraft(storeId,new CreateDemandDraftDTO(date,wednesdayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.THURSDAY){
                map.put(date,thursdayDraft);
                demandDraftService.createDemandDraft(storeId,new CreateDemandDraftDTO(date,thursdayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.FRIDAY){
                map.put(date,fridayDraft);
                demandDraftService.createDemandDraft(storeId,new CreateDemandDraftDTO(date,fridayDraft));
            }

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY){
                map.put(date,saturdayDraft);
                demandDraftService.createDemandDraft(storeId,new CreateDemandDraftDTO(date,saturdayDraft));
            }
        }

        return map;
    }

    private Map<LocalDate, OpenCloseStoreHoursDTO> getStoreOpenCloseHour(Integer year, Integer month){
        Map<LocalDate, OpenCloseStoreHoursDTO> map = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(year,month);
        for (int day = 1; day <= yearMonth.lengthOfMonth();day++){
            LocalDate date = LocalDate.of(year,month,day);

            if (date.getDayOfWeek() != DayOfWeek.SUNDAY){
                map.put(date,new OpenCloseStoreHoursDTO(8,20));
            }
        }

        return map;
    }


    private List<Employee> getEmployees(){
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

        return employeeEntityService.saveAll(List.of(
                damMro,
                monBar,
                matKru,
                filKam,
                marNow,
                wojPie,
                micWoc,
                tomZaj,
                agaWar,
                micKoz,
                marPrz,
                marWoj,
                olgDar,
                karNak,
                emiMia
        ));
    }
}