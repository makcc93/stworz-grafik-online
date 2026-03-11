package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VacationApplier {
    private final ScheduleDetailsService scheduleDetailsService;
    private final HolidayManager holidayManager;

    public void applyVacationsToSchedule(ScheduleGeneratorContext context){
        List<Employee> employeesWithVacation = context.getStoreActiveEmployees().stream()
                .filter(context::employeeHasPlannedVacation)
                .toList();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
                continue;
            }

            for (Employee employee : employeesWithVacation) {
                if (context.employeeIsOnVacation(employee, day)) {
                    registerVacationOnSchedule(context, employee, date);

                    context.addEmployeeHours(employee,context.getDefaultVacationShift());
                    context.addEmployeeVacationDay(employee,1);
                }
            }

        }
    }

    private void registerVacationOnSchedule(ScheduleGeneratorContext context, Employee employee, LocalDate date) {
        scheduleDetailsService.addScheduleDetails(
                context.getStoreId(),
                context.getSchedule().getId(),
                new CreateScheduleDetailsDTO(
                        employee.getId(),
                        date,
                        context.getDefaultVacationShift().getId(),
                        context.getVacationShiftTypeConfig().getId()
                )
        );
    }
}
