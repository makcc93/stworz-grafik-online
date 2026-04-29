package online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class OpenCloseMatcher extends AbstractRoleMatcher{
    @Override
    protected boolean specialCheckoutRule() {
        return false;
    }

    @Override
    protected void performSave(ScheduleGeneratorContext context, LocalDate date, Employee employee, Shift shift) {
        context.assignEmployeeToOpenClose(date,employee,shift);
    }

    @Override
    protected boolean morningHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray) {
        int openHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).openHour();

        return (shiftAsArray[openHour] > 0);
    }

    @Override
    protected boolean afternoonHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray) {
        int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();

        return shiftAsArray[closeHour] > 0;
    }

    @Override
    protected Comparator<Employee> getSortingRules(ScheduleGeneratorContext context, LocalDate date) {
        return Comparator.comparing(Employee::isManager, Comparator.reverseOrder());
    }

    @Override
    protected List<Employee> getFilteredEmployees(ScheduleGeneratorContext context, LocalDate date) {
        return context.getFinalSchedule().getOrDefault(date, Map.of()).keySet().stream()
                .filter(Employee::isCanOpenCloseStore)
                .filter(empl -> context.employeeIsWorking(empl, date))
                .filter(empl -> !context.employeeHasProposalDaysOff(empl, date))
                .toList();
    }
}
