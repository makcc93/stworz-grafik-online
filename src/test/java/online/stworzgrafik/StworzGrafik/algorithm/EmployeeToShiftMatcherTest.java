package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.TestShiftBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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
        when(calendarCalculation.getMonthlyMaxWorkingDays(2026, 3)).thenReturn(22);
        when(calendarCalculation.getMonthlyStandardWorkingHours(2026, 3)).thenReturn(160);
        when(holidayManager.isHoliday(DAY)).thenReturn(false);
    }

    @Test
    void matchEmployeeToShift_workingTest(){
        //given
        int[] demandDraft = {0,0,0,0,0,0,0,0,2,4,7,7,6,6,8,8,8,8,8,4,0,0,0,0};
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
        matcher.matchEmployeeToShift(context);

        //then
        ArgumentCaptor<CreateScheduleDetailsDTO> captor =
                ArgumentCaptor.forClass(CreateScheduleDetailsDTO.class);

        List<CreateScheduleDetailsDTO> registeredShifts = captor.getAllValues();

        verify(scheduleMessageService, never()).addMessage(any(), argThat(dto ->
                dto.scheduleMessageType() == ScheduleMessageType.ERROR
        ));

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
