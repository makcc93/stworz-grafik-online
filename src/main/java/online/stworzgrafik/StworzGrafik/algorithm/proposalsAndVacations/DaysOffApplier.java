package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DaysOffApplier {
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;

    public void applyDaysOffToSchedule(ScheduleGeneratorContext context){
        List<Employee> employeesWithProposalDaysOff= context.getStoreActiveEmployees().stream()
                .filter(context::employeeHasProposalDaysOff)
                .toList();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date)){
                continue;
            }

            for (Employee employee : employeesWithProposalDaysOff){
                registerDayOffOnSchedule(context,employee,date);
            }
        }
    }

    private void registerDayOffOnSchedule(ScheduleGeneratorContext context, Employee employee, LocalDate date) {
        scheduleDetailsService.addScheduleDetails(
                context.getStoreId(),
                context.getSchedule().getId(),
                new CreateScheduleDetailsDTO(
                        employee.getId(),
                        date,
                        context.getDefaultDaysOffShift().getId(),
                        context.getDaysOffShiftTypeConfig().getId()
                )
        );
    }
}
