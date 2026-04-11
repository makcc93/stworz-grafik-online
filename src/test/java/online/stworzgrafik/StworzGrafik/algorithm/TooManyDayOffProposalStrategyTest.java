package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.algorithm.analyzer.TooManyDayOffProposalResult;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.TooManyDayOffProposalStrategy;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TooManyDayOffProposalStrategyTest {
    @InjectMocks
    private TooManyDayOffProposalStrategy strategy;

    @Mock
    private ScheduleMessageService scheduleMessageService;

    @Mock
    private ScheduleDetailsEntityService scheduleDetailsEntityService;

    @Mock
    private ScheduleDetailsService scheduleDetailsService;

    @Test
    void resolve_employeeWithMostDayOffProposalCountAddToAvailableEmployeeListAndCancelProposal(){
        //given
        LocalDate date = LocalDate.of(2026,3,2);
        Employee emp1 = mock(Employee.class);
        Employee emp2 = mock(Employee.class);
        Employee emp3 = mock(Employee.class);

        int[] emp1MonthlyDayOffProposal = {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] emp2MonthlyDayOffProposal = {0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] emp3MonthlyDayOffProposal = {0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        Schedule schedule = mock(Schedule.class);
        when(schedule.getId()).thenReturn(1L);

        ScheduleGeneratorContext context = mock(ScheduleGeneratorContext.class);
        when(context.getSchedule()).thenReturn(schedule);
        when(context.getStoreId()).thenReturn(22L);

        List<Employee> availableEmployees = new ArrayList<>();
        List<Shift> shifts = List.of(mock(Shift.class));

        TooManyDayOffProposalResult result = new TooManyDayOffProposalResult(availableEmployees,shifts);

        Map<Employee, int[]> monthlyEmployeesProposalDayOff = new HashMap<>();
        monthlyEmployeesProposalDayOff.put(emp1,emp1MonthlyDayOffProposal);
        monthlyEmployeesProposalDayOff.put(emp2,emp2MonthlyDayOffProposal);
        monthlyEmployeesProposalDayOff.put(emp3,emp3MonthlyDayOffProposal);


        ScheduleDetails scheduleDetails = mock(ScheduleDetails.class);
        when(scheduleDetails.getId()).thenReturn(33L);
        when(context.getMonthlyEmployeesProposalDayOff()).thenReturn(monthlyEmployeesProposalDayOff);
        when(scheduleDetailsEntityService.findEmployeeScheduleDetailsByDay(anyLong(),anyLong(),eq(emp3),eq(date))).thenReturn(Optional.of(scheduleDetails));

        //when
        strategy.resolve(result,context,date);

        //then
        assertEquals(1,availableEmployees.size());
        assertTrue(availableEmployees.contains(emp3));

        verify(scheduleDetailsService).deleteScheduleDetails(eq(22L),eq(1L),eq(33L));
        verify(scheduleMessageService).addMessage(eq(1L), argThat( message ->
                message.scheduleMessageType() == ScheduleMessageType.INFO &&
                message.scheduleMessageCode() == ScheduleMessageCode.UNDERSTAFFED &&
                message.messageDate() == date));
    }
}
