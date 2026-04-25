package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseStoreHoursIndexDTO;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.TestScheduleBuilder;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.TestShiftTypeConfigBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyStoreScheduleGeneratorTest {
    @InjectMocks
    private MonthlyStoreScheduleGenerator monthlyStoreScheduleGenerator;

    @Mock
    private ScheduleGeneratorContext context;

    @Mock
    private ScheduleGeneratorContextFactory contextFactory;

    private final int year = 2026;
    private final int month = 3;
    private int[] mondayDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,9,9,9,9,9,5,0,0,0,0};
    private int[] tuesdayDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,8,8,8,8,8,4,0,0,0,0};
    private int[] wednesdayDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,7,7,7,7,7,4,0,0,0,0};
    private int[] thursdayDraft = {0,0,0,0,0,0,0,0,3,6,6,6,6,6,6,6,6,6,6,4,0,0,0,0};
    private int[] fridayDraft = {0,0,0,0,0,0,0,0,3,6,7,7,8,8,9,9,9,9,9,6,0,0,0,0};
    private int[] saturdayDraft = {0,0,0,0,0,0,0,0,3,6,10,10,10,10,10,9,9,9,9,6,0,0,0,0};
    private Shift defaultVacationShift = new TestShiftBuilder().withStartHour(LocalTime.of(12,0)).withEndHour(LocalTime.of(20,0)).build();
    private Shift defaultDayOffShift = new TestShiftBuilder().withStartHour(LocalTime.of(0,0)).withEndHour(LocalTime.of(0,0)).build();


    private Region region;
    private Branch branch;
    private Store store;
    private Schedule schedule;


    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();
        schedule = new TestScheduleBuilder().withRegion(region).withBranch(branch).withStore(store).withYear(year).withMonth(month).build();

        when(context.getStoreId()).thenReturn(store.getId());
        when(context.getYear()).thenReturn(year);
        when(context.getMonth()).thenReturn(month);
        when(context.getSchedule()).thenReturn(schedule);
        when(context.getStore()).thenReturn(store);
        when(context.getStoreOpenCloseHoursForEmployeesByDate()).thenReturn(getStoreOpenCloseHour(year,month));
        when(context.getStoreActiveEmployees()).thenReturn(getEmployees());
        when(context.getUneditedOriginalDateStoreDraft()).thenReturn(getDraftForEveryDay(year,month));
        when(context.getEveryDayStoreDemandDraftWorkingOn()).thenReturn(getSortedDrafts(year,month));
        when(context.getMonthlyEmployeesProposalShiftsByDate()).thenReturn(new HashMap<>());
        when(context.getMonthlyEmployeesProposalDayOff()).thenReturn(new HashMap<>());
        when(context.getMonthlyEmployeesVacation()).thenReturn(new HashMap<>());
        when(context.getEmployeeHours()).thenReturn(new HashMap<>());
        when(context.getWorkingDaysCount()).thenReturn(new HashMap<>());
        when(context.getVacationDaysCount()).thenReturn(new HashMap<>());
        when(context.getGeneratedShiftsByDay()).thenReturn(getShiftsForEveryDay(year,month));
        when(context.getEmployeeWarehouseDays()).thenReturn(new HashMap<>());
        when(context.getEmployeeCreditDays()).thenReturn(new HashMap<>());
        when(context.getDefaultVacationShift()).thenReturn(defaultVacationShift);
        when(context.getDefaultDaysOffShift()).thenReturn(defaultDayOffShift);
        when(context.getAllShifts()).thenReturn(generateAllShifts());
        when(context.getVacationShiftTypeConfig()).thenReturn(new TestShiftTypeConfigBuilder().withCode(ShiftCode.VACATION).build());
        when(context.getDaysOffShiftTypeConfig()).thenReturn(new TestShiftTypeConfigBuilder().withCode(ShiftCode.DAY_OFF).build());
        when(context.getProposalShiftTypeConfig()).thenReturn(new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK_BY_PROPOSAL).build());
        when(context.getStandardShiftTypeConfig()).thenReturn(new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK).build());
        when(context.getFinalSchedule()).thenReturn(new LinkedHashMap<>());
        when(context.getFinalScheduleMessages()).thenReturn(new ArrayList<>());
        when(context.isStoreHasDedicatedWarehouseman()).thenReturn(true);

        when(contextFactory.create(any(),any(),any())).thenReturn(context);
    }

    //todo zmien na IT zeby sprawdzic czy grafik powstanie a jak tak to jak to wyglada
    @Test
    void generateMonthlySchedule_workingTest() throws IOException {
        //given

        //when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        //then
    }

    private List<Employee> getEmployees(){
        return List.of(
                new TestEmployeeBuilder().withFirstName("Damian").withLastName("Mrozicki").withSap(10000001L).withCanOpenCloseStore(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Monika").withLastName("Baran").withSap(10000002L).withCanOpenCloseStore(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Mateusz").withLastName("Kruk").withSap(10000003L).withCanOpenCloseStore(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Filip").withLastName("Kamiński").withSap(10000004L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Martyna").withLastName("Nowicka").withSap(10000005L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Wojciech").withLastName("Pietruszka").withSap(10000006L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Michał").withLastName("Woch").withSap(10000007L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Tomasz").withLastName("Zając").withSap(10000008L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Agata").withLastName("Warmińska").withSap(10000009L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Michał").withLastName("Kozik").withSap(10000010L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Przepiórka").withSap(10000011L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Wojtas").withSap(10000012L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Olga").withLastName("Beznazwiska").withSap(10000013L).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Karolina").withLastName("Nakonieczna").withSap(10000014L).withCashier(true).withCanOperateCheckout(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Emil").withLastName("Miazek").withSap(10000015L).withWarehouseman(true).withStore(store).build()
        );
    }

    private Map<LocalDate, OpenCloseStoreHoursIndexDTO> getStoreOpenCloseHour(Integer year, Integer month){
        Map<LocalDate, OpenCloseStoreHoursIndexDTO> map = new HashMap<>();

        YearMonth yearMonth = YearMonth.of(year,month);
        for (int day = 1; day <= yearMonth.lengthOfMonth();day++){
            LocalDate date = LocalDate.of(year,month,day);

            if (date.getDayOfWeek() != DayOfWeek.SUNDAY){
                map.put(date,new OpenCloseStoreHoursIndexDTO(8,20));
            }
        }

        return map;
    }

    private Map<LocalDate, int[]> getDraftForEveryDay(Integer year, Integer month){
        Map<LocalDate,int[]> map = new HashMap<>();


        YearMonth yearMonth = YearMonth.of(year,month);
        for (int day = 1; day <= yearMonth.lengthOfMonth();day++){
            LocalDate date = LocalDate.of(year,month,day);

            if (date.getDayOfWeek() == DayOfWeek.MONDAY){
                map.put(date,mondayDraft);
            }

            if (date.getDayOfWeek() == DayOfWeek.TUESDAY){
                map.put(date,tuesdayDraft);
            }

            if (date.getDayOfWeek() == DayOfWeek.WEDNESDAY){
                map.put(date,wednesdayDraft);
            }

            if (date.getDayOfWeek() == DayOfWeek.THURSDAY){
                map.put(date,thursdayDraft);
            }

            if (date.getDayOfWeek() == DayOfWeek.FRIDAY){
                map.put(date,fridayDraft);
            }

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY){
                map.put(date,saturdayDraft);
            }
        }

        return map;
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

        return shifts;
    }

}