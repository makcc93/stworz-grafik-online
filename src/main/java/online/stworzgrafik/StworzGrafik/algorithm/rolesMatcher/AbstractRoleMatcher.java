package online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher;

import com.mysql.cj.log.Log;
import lombok.extern.flogger.Flogger;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractRoleMatcher implements RoleMatcher{

    @Override
    public void assignForMonth(ScheduleGeneratorContext context) {
        for (int day = 1; day <= YearMonth.of(context.getYear(), context.getMonth()).lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);
            match(context, date);
        }
    }

    @Override
    public void match(ScheduleGeneratorContext context, LocalDate date){
        List<Employee> employees = getFilteredEmployees(context, date).stream()
                .sorted(getSortingRules(context,date))
                .toList();

        boolean morningAssigned = false;
        boolean afternoonAssigned = false;

        for (Employee employee : employees){
            if (morningAssigned && afternoonAssigned) break;

            Shift shift = context.getFinalSchedule().getOrDefault(date, Map.of()).get(employee);

            if (shift == null) continue;

            int[] shiftAsArray = context.shiftAsArray(shift);

            if (!morningAssigned && morningHoursCondition(context,date, shiftAsArray)){
                performSave(context,date,employee,shift);
                morningAssigned = true;

                log.info("****** {} DOPISUJE PRACOWNIKA {} NA PORANNĄ ZMIANĘ {}-{}",date, employee.getLastName(),shift.getStartHour(),shift.getEndHour());

                if (specialCheckoutRule()){
                    if (afternoonHoursCondition(context,date,shiftAsArray)){
                        log.info("**************** {} PRACOWNIK {} BEDZIĘ MIAŁ CAŁY DZIEŃ",date,employee.getLastName());
                        afternoonAssigned = true;
                        break;
                    }
                }
                continue;
            }

            if (!afternoonAssigned && afternoonHoursCondition(context,date,shiftAsArray)){
                performSave(context,date,employee,shift);
                afternoonAssigned = true;

                log.info("****** {} DOPISUJE PRACOWNIKA {} NA POPOŁUDNIOWĄ ZMIANĘ {}-{}", date,employee.getLastName(),shift.getStartHour(),shift.getEndHour());
            }
        }
    }

    protected abstract boolean specialCheckoutRule();
    protected abstract void performSave(ScheduleGeneratorContext context, LocalDate date, Employee employee, Shift shift);
    protected abstract boolean morningHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray);
    protected abstract boolean afternoonHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray);
    protected abstract Comparator<Employee> getSortingRules(ScheduleGeneratorContext context, LocalDate date);
    protected abstract List<Employee> getFilteredEmployees(ScheduleGeneratorContext context, LocalDate date);
}
