package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class EmptyDaysMatcher {
    public void completeEmptyDaysWithDayOffShift(ScheduleGeneratorContext context){
        log.info("UZUPEŁNIAM BRAK ZMIANY DNIAMI WOLNYMI");
        LinkedHashMap<LocalDate, Map<Employee, Shift>> finalSchedule = context.getFinalSchedule();
        YearMonth yearMonth = YearMonth.of(context.getYear(),context.getMonth());

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(context.getYear(),context.getMonth(),day);

            if (!finalSchedule.containsKey(date)){
                for (Employee employee : context.getStoreNotSpecialActiveEmployees()){
                    context.registerShiftOnSchedule(date,employee,context.getDefaultDaysOffShift(),date.getDayOfWeek());
                }
                continue;
            }

            for (Employee employee : context.getStoreNotSpecialActiveEmployees()){
                if (context.employeeIsOnVacation(employee,date)) continue;
                if (context.employeeIsOnDelegation(employee,date)) continue;

                if (!finalSchedule.getOrDefault(date,Map.of()).containsKey(employee)){
                    context.registerShiftOnSchedule(date,employee,context.getDefaultDaysOffShift(),date.getDayOfWeek());
                }
            }
        }
    }
}
