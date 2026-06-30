package online.stworzgrafik.StworzGrafik.algorithm.preparation;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
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
        log.info("WPROWADZENIE URLOPÓW");
        List<Employee> employeesWithVacation = context.getStoreAllActiveEmployees().stream()
                .filter(context::employeeHasPlannedVacation)
                .toList();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        Shift vacationShift = context.getDefaultVacationShift();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            for (Employee employee : employeesWithVacation) {
                if (holidayManager.isHoliday(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
                    context.addEmployeeDayOffProposal(date,employee);
                    continue;
                }

                if (context.employeeIsOnVacation(employee, date)) {
                    context.registerShiftOnSchedule(date,employee,vacationShift,date.getDayOfWeek());
                    context.addEmployeeVacationDay(employee,1);
                }
            }
        }
    }
}
