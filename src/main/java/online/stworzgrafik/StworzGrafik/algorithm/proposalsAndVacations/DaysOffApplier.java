package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DaysOffApplier {
    private final HolidayManager holidayManager;

    public void applyDaysOffToSchedule(ScheduleGeneratorContext context){
        log.info("Sprawdzam propozycje dni wolnych do dodania do grafika");

        List<Employee> employeesWithProposalDaysOff= context.getStoreActiveEmployees().stream()
                .filter(context::employeeHasProposalDaysOff)
                .toList();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        Shift dayOffShift = context.getDefaultDaysOffShift();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date)){
                continue;
            }

            for (Employee employee : employeesWithProposalDaysOff){
                log.info("Wprowadzam propozycję dnia wolnego dla {} {} w dniu {}", employee.getFirstName(),employee.getLastName(),date);

                context.registerShiftOnSchedule(date,employee,dayOffShift);
            }
        }
    }
}
