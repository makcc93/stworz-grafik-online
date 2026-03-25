package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.algorithm.analyzer.TooManyProposalsAnalysisResult;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.TooManyProposalsAnalysisStrategy;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TooManyProposalsAnalysisStrategyTest {
    @InjectMocks
    private TooManyProposalsAnalysisStrategy strategy;

    @Mock
    private ScheduleMessageService scheduleMessageService;

    @Test
    void resolve_reduceProposalOnEmployeeWhoCantOpenStoreWithHighestHours(){
        // given
        LocalDate date = LocalDate.of(2024, 5, 20);
        int targetHour = 8;

        Employee emp1 = mock(Employee.class);
        Employee emp2 = mock(Employee.class);
        Employee empCantOpenStoreWithHighestHours = mock(Employee.class);
        Employee emp4 = mock(Employee.class);
        when(emp1.isCanOpenCloseStore()).thenReturn(true);
        when(emp2.isCanOpenCloseStore()).thenReturn(false);
        when(empCantOpenStoreWithHighestHours.isCanOpenCloseStore()).thenReturn(false);
        when(emp4.isCanOpenCloseStore()).thenReturn(false);

        int[] originalDailyDraft = new int[24];
        originalDailyDraft[targetHour] = 3;

        int[] proposalCount = new int[24];
        proposalCount[targetHour] = 4;

        TooManyProposalsAnalysisResult result = new TooManyProposalsAnalysisResult(originalDailyDraft, proposalCount);

        ScheduleGeneratorContext context = mock(ScheduleGeneratorContext.class);
        when(context.getStoreActiveEmployees()).thenReturn(List.of(emp1, emp2,empCantOpenStoreWithHighestHours, emp4));

        Map<Employee, Integer> employeeHours = new HashMap<>();
        employeeHours.put(emp1, 100);
        employeeHours.put(emp2, 15);
        employeeHours.put(empCantOpenStoreWithHighestHours, 95);
        employeeHours.put(emp4, 35);
        when(context.getEmployeeHours()).thenReturn(employeeHours);

        int[] emp1Proposal = new int[24]; emp1Proposal[targetHour] = 1;
        int[] emp2Proposal = new int[24]; emp2Proposal[targetHour] = 1;
        int[] empCantOpenStoreWithHighestHoursProposal = new int[24]; empCantOpenStoreWithHighestHoursProposal[targetHour] = 1;
        int[] emp4Proposal = new int[24]; emp4Proposal[targetHour] = 1;

        Map<Employee, int[]> proposalsMap = new HashMap<>();
        proposalsMap.put(emp1, emp1Proposal);
        proposalsMap.put(emp2, emp2Proposal);
        proposalsMap.put(empCantOpenStoreWithHighestHours, empCantOpenStoreWithHighestHoursProposal);
        proposalsMap.put(emp4, emp4Proposal);

        when(context.getMonthlyEmployeesProposalShiftsByDate()).thenReturn(Map.of(date, proposalsMap));

        // when
        strategy.resolve(result, context, date);

        // then
        verify(context).updateEmployeeDailyProposal(eq(empCantOpenStoreWithHighestHours), eq(date), any(int[].class));
        assertEquals(3, proposalCount[targetHour]);
    }

}