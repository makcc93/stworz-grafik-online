package online.stworzgrafik.StworzGrafik.algorithm;

import de.focus_shift.jollyday.core.HolidayManager;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.AnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmployeeToShiftMatcherTest {
    @Mock
    private HolidayManager holidayManager;

    @Mock
    private ScheduleDetailsService scheduleDetailsService;

    @Mock
    private ScheduleDetailsEntityService scheduleDetailsEntityService;

    @Mock
    private ScheduleMessageService scheduleMessageService;

    @Mock
    private CalendarCalculation calendarCalculation;

    @Mock
    private ShiftEntityService shiftEntityService;

    @Mock
    private ScheduleAnalyzer scheduleAnalyzer;


    @InjectMocks
    private EmployeeToShiftMatcher matcher;

    private final LocalDate DAY = LocalDate.of(2026,3,9);
    private final Store store = new TestStoreBuilder().build();

    @BeforeEach
    void setupCalendar(){
//        when(calendarCalculation.getMonthlyMaxWorkingDays(2026, 3)).thenReturn(22);
//        when(calendarCalculation.getMonthlyStandardWorkingHours(2026, 3)).thenReturn(160);
//        when(holidayManager.isHoliday(DAY)).thenReturn(false);
    }

    @Test
    void matchEmployeeToShift_doNotThrowException(){
        //given
        int[] demandDraft = {0,0,0,0,0,0,0,0,3,6,7,7,7,7,9,9,9,9,9,6,0,0,0,0};
        List<Shift> shifts = generateLowestPersonNeededDailyShifts(demandDraft);
        List<Employee> employees = generateEmployees();

       for (Shift shift : shifts){
           System.out.println(shift.getStartHour() + " - " + shift.getEndHour());
       }

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreActiveEmployees(employees)
                .withEveryDayStoreDemandDraftWorkingOn(linkedMapOfDraft(DAY, demandDraft))
                .withUneditedOriginalDateStoreDraft(Map.of(DAY, demandDraft))
                .withGeneratedShiftsByDay(Map.of(DAY, shifts))
                .withWorkingDaysCount(generateWorkingDaysCount(employees))
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(DAY,new HashMap<>()))
                .withEmployeeHours(generateEmployeeWorkingHours(employees))
                .build();

        //when
        assertDoesNotThrow(() -> matcher.matchEmployeeToShift(context));
    }

    @Test
    void matchEmployeeToShift_understaffedCaseCancelEmployeeDayOffProposalAndAddToAvailableList(){
        //
        int[] demandDraft = {0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,0};
        Shift firstShift820 = new TestShiftBuilder().withStartHour(LocalTime.of(8,0)).withEndHour(LocalTime.of(20,0)).build();
        Shift secondShift820 = new TestShiftBuilder().withStartHour(LocalTime.of(8,0)).withEndHour(LocalTime.of(20,0)).build();
        Shift thirdShift820 = new TestShiftBuilder().withStartHour(LocalTime.of(8,0)).withEndHour(LocalTime.of(20,0)).build();

        List<Shift> shifts = List.of(firstShift820,secondShift820,thirdShift820);

        for (Shift shift : shifts){
            System.out.println(shift.getStartHour() + " - " + shift.getEndHour());
        }

        Employee firstManager = new TestEmployeeBuilder().withFirstName("Damian").withLastName("Mrozicki").withSap(10000001L).withCanOpenCloseStore(true).withStore(store).build();
        Employee secondManager = new TestEmployeeBuilder().withFirstName("Monika").withLastName("Baran").withSap(10000002L).withCanOpenCloseStore(true).withStore(store).build();
        Employee thirdManager = new TestEmployeeBuilder().withFirstName("Mateusz").withLastName("Kruk").withSap(10000003L).withCanOpenCloseStore(true).withStore(store).build();

        List<Employee> employees = List.of(firstManager,secondManager);

        Map<Employee,int[]> monthlyProposals = new HashMap<>();
        int[] thirdEmployeeDaysOffProposal = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        monthlyProposals.put(thirdManager, thirdEmployeeDaysOffProposal);

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreActiveEmployees(employees)
                .withEveryDayStoreDemandDraftWorkingOn(linkedMapOfDraft(DAY, demandDraft))
                .withUneditedOriginalDateStoreDraft(Map.of(DAY, demandDraft))
                .withGeneratedShiftsByDay(Map.of(DAY, shifts))
                .withWorkingDaysCount(generateWorkingDaysCount(employees))
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(DAY,new HashMap<>()))
                .withEmployeeHours(generateEmployeeWorkingHours(employees))
                .withMonthlyEmployeesProposalDayOff(monthlyProposals)
                .build();

        //when
        matcher.matchEmployeeToShift(context);

        //then
        verify(scheduleAnalyzer).analyzeAndResolve(
                eq(context),
                eq(DAY),
                any(),
                any(),
                eq(AnalyzeType.TOO_MANY_DAY_OFF_PROPOSALS)
        );
    }

    @Test
    void matchEmployeeToShift_tooFewEmployeeAvailableRunUnderstaffedAnalysis(){
        //given
        int[] demandDraft = {0,0,0,0,0,0,0,0,3,3,3,3,3,3,3,3,3,3,3,3,0,0,0,0};
        List<Shift> shifts = new ArrayList<>();
        for (Shift shift : generateLowestPersonNeededDailyShifts(demandDraft)) {
            shifts.add(shift);
        }

        List<Employee> employees = List.of(
                new TestEmployeeBuilder().withFirstName("Damian").withLastName("Mrozicki").withSap(10000001L).withCanOpenCloseStore(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Monika").withLastName("Baran").withSap(10000002L).withCanOpenCloseStore(true).withStore(store).build()
                );

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreActiveEmployees(employees)
                .withEveryDayStoreDemandDraftWorkingOn(linkedMapOfDraft(DAY, demandDraft))
                .withUneditedOriginalDateStoreDraft(Map.of(DAY, demandDraft))
                .withGeneratedShiftsByDay(Map.of(DAY, shifts))
                .withWorkingDaysCount(generateWorkingDaysCount(employees))
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(DAY,new HashMap<>()))
                .withEmployeeHours(generateEmployeeWorkingHours(employees))
                .build();

        //when
        matcher.matchEmployeeToShift(context);

        //then
        verify(scheduleAnalyzer).analyzeAndResolve(
                eq(context),
                eq(DAY),
                any(),
                any(),
                eq(AnalyzeType.TOO_MANY_DAY_OFF_PROPOSALS)
        );
    }

    @Test
    void matchEmployeeToShift_onlyOneAvailableManagerModifyHisScheduleFromOpenToCloseStore(){
        //given
        int[] demandDraft = {0,0,0,0,0,0,0,0,2,3,4,4,4,4,4,5,5,5,5,3,0,0,0,0};
        List<Shift> shifts = new ArrayList<>();
        for (Shift shift : generateLowestPersonNeededDailyShifts(demandDraft)) {
            shifts.add(shift);
            System.out.println(shift.getStartHour() + " - " + shift.getEndHour());
        }

        Employee theOnlyManager = new TestEmployeeBuilder().withFirstName("Damian").withCanOpenCloseStore(true).withStore(store).build();

        List<Employee> employees = List.of(
                theOnlyManager,
                new TestEmployeeBuilder().withFirstName("Olga").withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Marcin").withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Michał").withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Wojtek").withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Agata").withStore(store).build()
        );

        ScheduleGeneratorContext context = new TestScheduleGeneratorContext()
                .withStoreActiveEmployees(employees)
                .withEveryDayStoreDemandDraftWorkingOn(linkedMapOfDraft(DAY, demandDraft))
                .withUneditedOriginalDateStoreDraft(Map.of(DAY, demandDraft))
                .withGeneratedShiftsByDay(Map.of(DAY, shifts))
                .withWorkingDaysCount(generateWorkingDaysCount(employees))
                .withMonthlyEmployeesProposalShiftsByDate(Map.of(DAY,new HashMap<>()))
                .withEmployeeHours(generateEmployeeWorkingHours(employees))
                .build();

        //when
        matcher.matchEmployeeToShift(context);

        //then
    }

    private Map<Employee, Integer> generateEmployeeWorkingHours(List<Employee> employees){
        Map<Employee, Integer> map = new HashMap<>();

        for (Employee e : employees){
            map.put(e,50);
        }

        return map;
    }

    private Map<Employee,Integer> generateWorkingDaysCount(List<Employee> employees){
        Map<Employee, Integer> map = new HashMap<>();

        for (Employee e : employees){
            map.put(e,10);
        }

        return map;
    }

    private LinkedHashMap<LocalDate, int[]> linkedMapOfDraft(LocalDate DAY, int[] demandDraft){
        LinkedHashMap<LocalDate, int[]> map = new LinkedHashMap<>();
        map.put(DAY, demandDraft);
        return map;
    }

    private List<Employee> generateEmployees(){
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
}
