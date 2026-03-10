package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VacationApplier {
    private final ScheduleGeneratorContext context;
    private final ScheduleDetailsService scheduleDetailsService;
    private final HolidayManager holidayManager;

    public void applyVacationsToSchedule(ScheduleGeneratorContext context){
        Map<Employee, int[]> monthlyEmployeesVacation = context.getMonthlyEmployeesVacation();
        List<Employee> employees = context.getStoreActiveEmployees();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int day = 1; day <= yearMonth.getMonth().getValue(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
                continue;
            }

            for (Employee employee : employees) {
                if (context.employeeHasPlannedVacation(employee, year, month)) {
                    registerVacationOnSchedule(
                            context.getStoreId(),
                            context.getSchedule(),
                            employee,
                            date
                    );

                    context.addEmployeeHours(employee,context.getDefaultVacationShift());
                    //koncze tutaj, zastanawiam sie czy dodac nowa mape z liczba dni urlopowych i zapisac ta informacje
                }
            }

        }
    }

    private void registerVacationOnSchedule(Long storeId, Schedule schedule, Employee employee, LocalDate date) {
        scheduleDetailsService.addScheduleDetails(
                storeId,
                schedule.getId(),
                new CreateScheduleDetailsDTO(
                        employee.getId(),
                        date,
                        context.getDefaultVacationShift().getId(),
                        context.getVacationShiftTypeConfig().getId()
                )
        );
    }
}
