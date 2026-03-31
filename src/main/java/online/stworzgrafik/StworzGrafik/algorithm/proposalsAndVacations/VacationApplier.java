package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacationApplier {
    private final HolidayManager holidayManager;

    public void applyVacationsToSchedule(ScheduleGeneratorContext context){
        List<Employee> employeesWithVacation = context.getStoreActiveEmployees().stream()
                .filter(context::employeeHasPlannedVacation)
                .toList();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        Shift vacationShift = context.getDefaultVacationShift();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
                continue;
            }

            for (Employee employee : employeesWithVacation) {
                if (context.employeeIsOnVacation(employee, day)) {
                    log.info("Dodaje urlop pracownikowi {} {} w dniu {}",employee.getFirstName(),employee.getLastName(),date);

                    context.registerShiftOnSchedule(date,employee,vacationShift);
                    context.addWorkingInformation(employee,vacationShift,date.getDayOfWeek());
                }
            }

        }
    }
}
