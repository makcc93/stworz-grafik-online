package online.stworzgrafik.StworzGrafik.algorithm.preparation;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class DelegationApplier {
    private final HolidayManager holidayManager;

    public void applyDelegationToSchedule(ScheduleGeneratorContext context){
        log.info("WPROWADZENIE DELEGACJI");
        List<Employee> employeesWithDelegation = context.getStoreAllActiveEmployees().stream()
                .filter(context::employeeHasPlannedDelegation)
                .toList();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        Shift delegationShift = context.getDefaultDelegationShift();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date)){
                continue;
            }

            for (Employee employee : employeesWithDelegation) {
                if (context.employeeIsOnDelegation(employee, date)) {
                    Shift employeePossibleShift = context.getFinalSchedule().getOrDefault(date, Map.of()).getOrDefault(employee, context.getDefaultDaysOffShift());
                    if (context.getShiftLength(employeePossibleShift).compareTo(BigDecimal.ZERO) > 0){
                        context.deleteShiftFromSchedule(date,employee);
                    }

                    context.registerShiftOnSchedule(date,employee,delegationShift,date.getDayOfWeek());
                }
            }
        }
    }
}
