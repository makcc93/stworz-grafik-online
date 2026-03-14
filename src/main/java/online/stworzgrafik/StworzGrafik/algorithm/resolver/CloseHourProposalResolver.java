package online.stworzgrafik.StworzGrafik.algorithm.resolver;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.OpenCloseHourProposalResult;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloseHourProposalResolver {
    private final ShiftEntityService shiftEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final ScheduleDetailsService scheduleDetailsService;

    public void resolve(OpenCloseHourProposalResult result, ScheduleGeneratorContext context, LocalDate day, List<Shift> shiftsSorted){
        Map<Employee, int[]> dailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().get(day);

        Employee employeeWithHighestMonthlyWorkingHours = result.employeesWithOpenCloseStoreProposals().stream()
                .sorted(Comparator.comparingInt(empl -> context.getEmployeeHours().get(empl))
                        .reversed())
                .toList()
                .getFirst();

        Shift originalProposalShift = shiftEntityService.getArrayAsShift(dailyProposals.get(employeeWithHighestMonthlyWorkingHours));
        Shift endHourDecrementShift = shiftEntityService.getEntityByHours(originalProposalShift.getStartHour(),originalProposalShift.getEndHour().minusHours(1));

        ScheduleDetails employeeOldShiftInSchedule = scheduleDetailsEntityService.findEmployeeShiftByDay(context.getStoreId(), context.getSchedule().getId(), employeeWithHighestMonthlyWorkingHours, day);

        changeProposalShiftInSchedule(context,employeeOldShiftInSchedule,endHourDecrementShift);

        context.updateEmployeeDailyProposal(employeeWithHighestMonthlyWorkingHours,day, shiftEntityService.getShiftAsArray(endHourDecrementShift));

        updateShiftsInMatcher(shiftsSorted);
    }

    private static void updateShiftsInMatcher(List<Shift> shiftsSorted) {
        Shift shiftToChangeEndHour = shiftsSorted.stream()
                .sorted(longestCloseStoreShift())
                .toList()
                .getFirst();

        shiftToChangeEndHour.setEndHour(shiftToChangeEndHour.getEndHour().plusHours(1));
    }

    private void changeProposalShiftInSchedule(ScheduleGeneratorContext context, ScheduleDetails employeeOldShiftInSchedule, Shift endHourDecrementShift) {
        scheduleDetailsService.updateScheduleDetails(context.getStoreId(), context.getSchedule().getId(), employeeOldShiftInSchedule.getId(),
                new UpdateScheduleDetailsDTO(
                        null,
                        null,
                        endHourDecrementShift.getId(),
                        null
                )
        );

        context.updateEmployeeHours(employeeOldShiftInSchedule.getEmployee(),employeeOldShiftInSchedule.getShift(),endHourDecrementShift);
    }

    private static Comparator<Shift> longestCloseStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getEndHour().getHour()
                )
                .thenComparing(
                        Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ).reversed()
                );
    }
}
