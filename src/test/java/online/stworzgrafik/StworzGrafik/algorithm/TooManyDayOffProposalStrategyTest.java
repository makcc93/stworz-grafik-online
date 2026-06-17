package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.TooManyDayOffProposalResult;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.TooManyDayOffProposalStrategy;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @Test
    void resolve_employeeWithMostDayOffProposalCountAddToAvailableEmployeeListAndCancelProposal() {
        // given
        LocalDate date = LocalDate.of(2026, 3, 2);
        Employee emp1 = mock(Employee.class);
        Employee emp2 = mock(Employee.class);
        Employee emp3 = mock(Employee.class);

        int[] emp1MonthlyDayOffProposal = {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] emp2MonthlyDayOffProposal = {0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int[] emp3MonthlyDayOffProposal = {0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        when(emp1.isWarehouseman()).thenReturn(false);
        when(emp2.isWarehouseman()).thenReturn(false);
        when(emp3.isWarehouseman()).thenReturn(false);

        ScheduleGeneratorContext context = mock(ScheduleGeneratorContext.class);

        List<Employee> availableEmployees = new ArrayList<>();
        List<Shift> shifts = List.of(mock(Shift.class));

        TooManyDayOffProposalResult result = new TooManyDayOffProposalResult(availableEmployees, shifts);

        Map<Employee, int[]> monthlyEmployeesProposalDayOff = new HashMap<>();
        monthlyEmployeesProposalDayOff.put(emp1, emp1MonthlyDayOffProposal);
        monthlyEmployeesProposalDayOff.put(emp2, emp2MonthlyDayOffProposal);
        monthlyEmployeesProposalDayOff.put(emp3, emp3MonthlyDayOffProposal);

        when(context.getMonthlyEmployeesProposalDayOff()).thenReturn(monthlyEmployeesProposalDayOff);

        // when
        strategy.resolve(result, context, date);

        // then
        assertEquals(1, availableEmployees.size());
        assertTrue(availableEmployees.contains(emp3));

        verify(context).deleteShiftFromSchedule(eq(date), eq(emp3));
        verify(context).deleteEmployeeDayOffProposal(eq(date), eq(emp3));
        verify(context).registerMessageOnSchedule(argThat(message ->
                message.scheduleMessageType() == ScheduleMessageType.INFO &&
                        message.scheduleMessageCode() == ScheduleMessageCode.UNDERSTAFFED &&
                        message.messageDate().equals(date)
        ));

        verify(context, never()).deleteShiftFromSchedule(eq(date), eq(emp1));
        verify(context, never()).deleteShiftFromSchedule(eq(date), eq(emp2));
    }
}