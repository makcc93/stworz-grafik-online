package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.TestDatabaseCleaner;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseStoreHoursIndexDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ManagerOpeningHourAnalysisStrategy;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.TooManyDayOffProposalStrategy;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffService;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleService;
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class EmployeeToShiftMatcherIT {
    @Autowired
    private TestDatabaseCleaner cleaner;

    @Autowired
    private RegionEntityService regionEntityService;

    @Autowired
    private EmployeeEntityService employeeEntityService;

    @Autowired
    private EmployeeToShiftMatcher employeeToShiftMatcher;

    @Autowired
    private BranchEntityService branchEntityService;

    @Autowired
    private StoreEntityService storeEntityService;

    @Autowired
    private ShiftEntityService shiftEntityService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DemandDraftEntityService demandDraftEntityService;

    @Autowired
    private ScheduleDetailsEntityService scheduleDetailsEntityService;

    @Autowired
    private PositionEntityService positionEntityService;

    @Autowired
    private CalendarCalculation calendarCalculation;

    @Autowired
    private ShiftTypeConfigService shiftTypeConfigService;

    @Autowired
    private ManagerOpeningHourAnalysisStrategy managerOpeningHourAnalysisStrategy;

    @Autowired
    private ScheduleAnalyzer analyzer;

    @Autowired
    private TooManyDayOffProposalStrategy tooManyDayOffProposalStrategy;

    @Autowired
    private EmployeeProposalDaysOffService employeeProposalDaysOffService;

    @Autowired
    private ScheduleMessageService scheduleMessageService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    private Region region;
    private Branch branch;
    private Store store;
    private Schedule schedule;
    private Position position;
    private int year = 2026;
    private int month = 3;
    private List<Employee> employees;
    private ShiftTypeConfig standardWorkShiftTypeConfig;
    private ShiftTypeConfig workByProposalShiftTypeConfig;
    private ShiftTypeConfig dayOffShiftTypeConfig;
    private ShiftTypeConfig vacationShiftTypeConfig;


    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        regionEntityService.saveEntity(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchEntityService.saveEntity(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeEntityService.saveEntity(store);

        when(userAuthorizationService.hasAccessToStore(anyLong())).thenReturn(true);
        when(userAuthorizationService.getUserStoreId()).thenReturn(store.getId());

        schedule = new TestScheduleBuilder().withStore(store).withBranch(branch).withYear(year).withMonth(month).withRegion(region).build();
        scheduleService.saveSchedule(schedule);

        position = new TestPositionBuilder().build();
        positionEntityService.saveEntity(position);

        generateAndSaveAllShifts(shiftEntityService);

        standardWorkShiftTypeConfig = shiftTypeConfigService.saveByShiftCode(ShiftCode.WORK);
        workByProposalShiftTypeConfig = shiftTypeConfigService.saveByShiftCode(ShiftCode.WORK_BY_PROPOSAL);
        dayOffShiftTypeConfig = shiftTypeConfigService.saveByShiftCode(ShiftCode.DAY_OFF);
        vacationShiftTypeConfig = shiftTypeConfigService.saveByShiftCode(ShiftCode.VACATION);
    }

    @AfterEach
    void clean(){
        cleaner.cleanAll();
    }

    @Test
    void matchEmployeeToShift_workingTest(){
        //given
        LocalDate date = LocalDate.of(year,month,26);
        employees = generateAndSaveAllEmployees(employeeEntityService);

        int[] originalDemandDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,9,9,9,9,9,6,0,0,0,0};
        int[] employeeProposalsCount = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] workingOnDemandDraft = subtractArrays(originalDemandDraft,employeeProposalsCount);

        List<Shift> earlierPreparedShifts = generateLowestPersonNeededDailyShifts(workingOnDemandDraft);
        Map<LocalDate, List<Shift>> generatedShiftsByAlgorithm = Map.of(date,earlierPreparedShifts);

        LinkedHashMap<LocalDate,int[]> monthlyDraft = new LinkedHashMap<>();
        monthlyDraft.put(date,workingOnDemandDraft);

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreId(store.getId())
                .withYear(year)
                .withMonth(month)
                .withSchedule(schedule)
                .withStore(store)
                .withEveryDayStoreDemandDraftWorkingOn(monthlyDraft)
                .withStoreActiveEmployees(employees)
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(date,new HashMap<>()))
                .withGeneratedShiftsByDay(generatedShiftsByAlgorithm)
                .withWorkingDaysCount(workingDaysCount(employees))
                .withUneditedOriginalDateStoreDraft(Map.of(date,originalDemandDraft))
                .withEmployeeHours(generateEmployeeHours(employees))
                .withStandardShiftTypeConfig(ShiftTypeConfig.builder().code(ShiftCode.WORK).build())
                .withStandardShiftTypeConfig(standardWorkShiftTypeConfig)
                .withProposalShiftTypeConfig(workByProposalShiftTypeConfig)
                .withDaysOffShiftTypeConfig(dayOffShiftTypeConfig)
                .withVacationShiftTypeConfig(vacationShiftTypeConfig)
                .withStoreOpenCloseHoursForEmployeesByDate(generateOpenCloseStoreHoursByDate(date))
                .build();


        //when
        employeeToShiftMatcher.matchEmployeeToShift(context);

        //then
        List<ScheduleDetails> savedDetails = scheduleDetailsEntityService.findDailyScheduleDetails(
                store.getId(),
                schedule.getId(),
                date
        );
        assertThat(savedDetails).isNotEmpty();
    }

    
    @Test
    void matchEmployeeToShift_onlyOneManagerAvailableGiveAllDaysShiftToHim(){
        //given
        LocalDate date = LocalDate.of(year,month,26);
        employees = generateAndSaveAllEmployees(employeeEntityService);

        Optional<Employee> theOnlyOneManager = employees.stream().filter(Employee::isCanOpenCloseStore).findFirst();

        List<Employee> employeesWithOnlyOneManager = new ArrayList<>();
        for (Employee e : employees){
            if (!e.isCanOpenCloseStore()){
                employeesWithOnlyOneManager.add(e);
            }
        }

        employeesWithOnlyOneManager.add(theOnlyOneManager.get());

        int[] originalDemandDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,9,9,9,9,9,6,0,0,0,0};
        int[] employeeProposalsCount = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] workingOnDemandDraft = subtractArrays(originalDemandDraft,employeeProposalsCount);

        List<Shift> earlierPreparedShifts = generateLowestPersonNeededDailyShifts(workingOnDemandDraft);
        Map<LocalDate, List<Shift>> generatedShiftsByAlgorithm = Map.of(date,earlierPreparedShifts);

        LinkedHashMap<LocalDate,int[]> monthlyDraft = new LinkedHashMap<>();
        monthlyDraft.put(date,workingOnDemandDraft);

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreId(store.getId())
                .withYear(year)
                .withMonth(month)
                .withSchedule(schedule)
                .withStore(store)
                .withEveryDayStoreDemandDraftWorkingOn(monthlyDraft)
                .withStoreActiveEmployees(employeesWithOnlyOneManager)
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(date,new HashMap<>()))
                .withGeneratedShiftsByDay(generatedShiftsByAlgorithm)
                .withWorkingDaysCount(workingDaysCount(employees))
                .withUneditedOriginalDateStoreDraft(Map.of(date,originalDemandDraft))
                .withEmployeeHours(generateEmployeeHours(employees))
                .withStandardShiftTypeConfig(ShiftTypeConfig.builder().code(ShiftCode.WORK).build())
                .withStandardShiftTypeConfig(standardWorkShiftTypeConfig)
                .withProposalShiftTypeConfig(workByProposalShiftTypeConfig)
                .withDaysOffShiftTypeConfig(dayOffShiftTypeConfig)
                .withVacationShiftTypeConfig(vacationShiftTypeConfig)
                .withStoreOpenCloseHoursForEmployeesByDate(generateOpenCloseStoreHoursByDate(date))
                .build();


        //when
        employeeToShiftMatcher.matchEmployeeToShift(context);

        //then
        List<ScheduleDetails> savedDetails = scheduleDetailsEntityService.findDailyScheduleDetails(
                store.getId(),
                schedule.getId(),
                date
        );
        assertThat(savedDetails).isNotEmpty();

        boolean isTheOnlyOneManagerWorking = savedDetails.stream()
                .anyMatch(sd -> sd.getEmployee().getLastName().equals(theOnlyOneManager.get().getLastName()));

        assertTrue(isTheOnlyOneManagerWorking);
    }

    
    @Test
    void matchEmployeeToShift_zeroDemandDraftDay() {
        //given
        LocalDate date = LocalDate.of(year, month, 22);
        employees = generateAndSaveAllEmployees(employeeEntityService);

        int[] originalDemandDraft = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] employeeProposalsCount = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] workingOnDemandDraft = subtractArrays(originalDemandDraft, employeeProposalsCount);

        List<Shift> earlierPreparedShifts = generateLowestPersonNeededDailyShifts(workingOnDemandDraft);
        Map<LocalDate, List<Shift>> generatedShiftsByAlgorithm = Map.of(date, earlierPreparedShifts);

        LinkedHashMap<LocalDate, int[]> monthlyDraft = new LinkedHashMap<>();
        monthlyDraft.put(date, workingOnDemandDraft);

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreId(store.getId())
                .withYear(year)
                .withMonth(month)
                .withSchedule(schedule)
                .withStore(store)
                .withEveryDayStoreDemandDraftWorkingOn(monthlyDraft)
                .withStoreActiveEmployees(employees)
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(date, new HashMap<>()))
                .withGeneratedShiftsByDay(generatedShiftsByAlgorithm)
                .withWorkingDaysCount(workingDaysCount(employees))
                .withUneditedOriginalDateStoreDraft(Map.of(date, originalDemandDraft))
                .withEmployeeHours(generateEmployeeHours(employees))
                .withStandardShiftTypeConfig(ShiftTypeConfig.builder().code(ShiftCode.WORK).build())
                .withStandardShiftTypeConfig(standardWorkShiftTypeConfig)
                .withProposalShiftTypeConfig(workByProposalShiftTypeConfig)
                .withDaysOffShiftTypeConfig(dayOffShiftTypeConfig)
                .withVacationShiftTypeConfig(vacationShiftTypeConfig)
                .withStoreOpenCloseHoursForEmployeesByDate(generateOpenCloseStoreHoursByDate(date))
                .build();


        //when
        employeeToShiftMatcher.matchEmployeeToShift(context);

        //then
        List<ScheduleDetails> savedDetails = scheduleDetailsEntityService.findDailyScheduleDetails(
                store.getId(),
                schedule.getId(),
                date
        );

        assertThat(savedDetails).isEmpty();
    }

    
    @Test
    void matchEmployeeToShift_understaffedAddEmployeeToAvailable(){ //todo Cannot find schedule details for schedule id...
        //given
        LocalDate date = LocalDate.of(year,month,26);
        employees = generateAndSaveAllEmployees(employeeEntityService);

        int[] originalDemandDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,9,9,9,9,9,6,0,0,0,0};
        int[] employeeProposalsCount = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] workingOnDemandDraft = subtractArrays(originalDemandDraft,employeeProposalsCount);

        List<Shift> earlierPreparedShifts = generateLowestPersonNeededDailyShifts(workingOnDemandDraft);
        Map<LocalDate, List<Shift>> generatedShiftsByAlgorithm = Map.of(date,earlierPreparedShifts);

        LinkedHashMap<LocalDate,int[]> monthlyDraft = new LinkedHashMap<>();
        monthlyDraft.put(date,workingOnDemandDraft);

        Map<Employee, int[]> employeesDaysOffProposal = new HashMap<>();
        int[] allMonthDayOffProposal = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        for (Employee e : employees){
            if (!e.isCanOpenCloseStore() && !e.isWarehouseman()){
                employeesDaysOffProposal.put(e,allMonthDayOffProposal);

                EmployeeProposalDaysOff dayOffProposal = EmployeeProposalDaysOff.builder().employee(e).monthlyDaysOff(allMonthDayOffProposal).store(store).year(year).month(month).build();
                employeeProposalDaysOffService.save(dayOffProposal);

                Shift dayOffShift = shiftEntityService.getEntityByHours(LocalTime.of(0, 0), LocalTime.of(0, 0));
                scheduleDetailsEntityService.add(store.getId(),schedule.getId(),new CreateScheduleDetailsDTO(e.getId(),date,dayOffShift.getId(),dayOffShiftTypeConfig.getId()));
            }
        }



        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreId(store.getId())
                .withYear(year)
                .withMonth(month)
                .withSchedule(schedule)
                .withStore(store)
                .withEveryDayStoreDemandDraftWorkingOn(monthlyDraft)
                .withStoreActiveEmployees(employees)
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(date,new HashMap<>()))
                .withGeneratedShiftsByDay(generatedShiftsByAlgorithm)
                .withWorkingDaysCount(workingDaysCount(employees))
                .withUneditedOriginalDateStoreDraft(Map.of(date,originalDemandDraft))
                .withEmployeeHours(generateEmployeeHours(employees))
                .withStandardShiftTypeConfig(ShiftTypeConfig.builder().code(ShiftCode.WORK).build())
                .withStandardShiftTypeConfig(standardWorkShiftTypeConfig)
                .withProposalShiftTypeConfig(workByProposalShiftTypeConfig)
                .withDaysOffShiftTypeConfig(dayOffShiftTypeConfig)
                .withVacationShiftTypeConfig(vacationShiftTypeConfig)
                .withStoreOpenCloseHoursForEmployeesByDate(generateOpenCloseStoreHoursByDate(date))
                .withMonthlyEmployeesProposalDayOff(employeesDaysOffProposal)
                .build();


        //when
        employeeToShiftMatcher.matchEmployeeToShift(context);

        //then
        List<ScheduleDetails> savedDetails = scheduleDetailsEntityService.findDailyScheduleDetails(
                store.getId(),
                schedule.getId(),
                date
        );
        assertThat(savedDetails).isNotEmpty();
    }

    private Map<LocalDate, OpenCloseStoreHoursIndexDTO> generateOpenCloseStoreHoursByDate(LocalDate date){
    Map<LocalDate, OpenCloseStoreHoursIndexDTO> map = new HashMap<>();
    map.put(date, new OpenCloseStoreHoursIndexDTO(8,20));

    return map;
    }

    private Map<Employee, Integer> generateEmployeeHours(List<Employee> employees){
        Map<Employee, Integer> map = new HashMap<>();

        for (Employee e : employees){
            map.put(e,0);
        }

        return map;
    }

    private int[] subtractArrays(int[] originalDemandDraft, int[] employeeProposalsCount){
        int[] result = new int[24];

        for (int i = 0; i < originalDemandDraft.length; i++){
            result[i] = originalDemandDraft[i] - employeeProposalsCount[i];
        }

        return result;
    }

    private Map<Employee, Integer> workingDaysCount(List<Employee> employees){
        Map<Employee, Integer> map = new HashMap<>();
        for (Employee e : employees){
            map.put(e,0);
        }

        return map;
    }

    private List<Employee> generateAndSaveAllEmployees(EmployeeEntityService employeeEntityService){
        Employee employee1 = new TestEmployeeBuilder().withFirstName("Damian").withLastName("Mrozicki").withSap(10000001L).withCanOpenCloseStore(true).withStore(this.store).withPosition(this.position).build();
        Employee employee2 = new TestEmployeeBuilder().withFirstName("Monika").withLastName("Baran").withSap(10000002L).withCanOpenCloseStore(true).withStore(this.store).withPosition(this.position).build();
        Employee employee3 = new TestEmployeeBuilder().withFirstName("Mateusz").withLastName("Kruk").withSap(10000003L).withCanOpenCloseStore(true).withStore(this.store).withPosition(this.position).build();
        Employee employee4 = new TestEmployeeBuilder().withFirstName("Filip").withLastName("Kamiński").withSap(10000004L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee5 = new TestEmployeeBuilder().withFirstName("Martyna").withLastName("Nowicka").withSap(10000005L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee6 = new TestEmployeeBuilder().withFirstName("Wojciech").withLastName("Pietruszka").withSap(10000006L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee7 = new TestEmployeeBuilder().withFirstName("Michał").withLastName("Woch").withSap(10000007L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee8 = new TestEmployeeBuilder().withFirstName("Tomasz").withLastName("Zając").withSap(10000008L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee9 = new TestEmployeeBuilder().withFirstName("Agata").withLastName("Warmińska").withSap(10000009L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee10 = new TestEmployeeBuilder().withFirstName("Michał").withLastName("Kozik").withSap(10000010L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee11 = new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Przepiórka").withSap(10000011L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee12 = new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Wojtas").withSap(10000012L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(this.store).withPosition(this.position).build();
        Employee employee13 = new TestEmployeeBuilder().withFirstName("Olga").withLastName("Beznazwiska").withSap(10000013L).withStore(this.store).withPosition(this.position).build();
        Employee employee14 = new TestEmployeeBuilder().withFirstName("Karolina").withLastName("Nakonieczna").withSap(10000014L).withCashier(true).withCanOperateCheckout(true).withStore(this.store).withPosition(this.position).build();
        Employee employee15 = new TestEmployeeBuilder().withFirstName("Emil").withLastName("Miazek").withSap(10000015L).withWarehouseman(true).withStore(this.store).withPosition(this.position).build();

        List<Employee> employees = new ArrayList<>(List.of(employee1, employee2, employee3, employee4, employee5, employee6, employee7, employee8, employee9, employee10, employee11, employee12, employee13, employee14, employee15));
        employeeEntityService.saveAll(employees);

        return employees;
    }

    private void generateAndSaveAllShifts(ShiftEntityService shiftEntityService) {
        List<Shift> shifts = new ArrayList<>();
        for (int startHour = 0; startHour <= 23; startHour++) {
            LocalTime start = LocalTime.of(startHour, 0);
            for (int endHour = 0; endHour <= 23; endHour++) {
                LocalTime end = LocalTime.of(endHour, 0);
                Shift shift = Shift.builder()
                        .startHour(start)
                        .endHour(end)
                        .build();

                shifts.add(shift);
            }
        }

        shiftEntityService.saveAll(shifts);
    }

    private List<Shift> generateLowestPersonNeededDailyShifts(int[] dailyDemandDraft) {
        List<int[]> startEndPairs = new ArrayList<>();

        // Wyznacz godziny startu
        List<Integer> startHours = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            int demand = dailyDemandDraft[hour];
            if (demand > 0) {
                int previousDemand = (hour == 0) ? 0 : dailyDemandDraft[hour - 1];
                for (int i = demand; i > previousDemand; i--) {
                    startHours.add(hour);
                }
            }
        }

        // Posortuj malejąco żeby dopasować do godzin końca
        startHours.sort(Collections.reverseOrder());

        // Wyznacz godziny końca i paruj ze startami
        int index = 0;
        for (int hour = 23; hour >= 0; hour--) {
            int demand = dailyDemandDraft[hour];
            if (demand > 0) {
                int nextDemand = (hour == 23) ? 0 : dailyDemandDraft[hour + 1];
                for (int i = demand; i > nextDemand; i--) {
                    int endHour = (hour == 23) ? 0 : hour + 1;
                    startEndPairs.add(new int[]{startHours.get(index), endHour});
                    index++;
                }
            }
        }

        // Pobierz encje z bazy po gotowych parach godzin
        return startEndPairs.stream()
                .map(pair -> shiftEntityService.getEntityByHours(
                        LocalTime.of(pair[0], 0),
                        LocalTime.of(pair[1], 0)
                ))
                .collect(Collectors.toList());
    }

//    private List<Shift> generateLowestPersonNeededDailyShifts(int[] dailyDemandDraft) {
//        List<Shift> startHoursShifts = generateShiftStartHours(dailyDemandDraft);
//
//        List<Shift> shiftsSortedDesc = startHoursShifts.stream()
//                .sorted(Comparator.comparing(Shift::getStartHour).reversed())
//                .toList();
//
//        return generateShiftEndHours(shiftsSortedDesc, dailyDemandDraft);
//    }

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
                    Shift shift = new Shift();
                    shift.setStartHour(LocalTime.of(hourOfDay,0));

                    shifts.add(shift);
                }
            }
        }
        return shifts;
    }
}
