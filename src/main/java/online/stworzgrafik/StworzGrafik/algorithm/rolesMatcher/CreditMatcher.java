package online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CreditMatcher extends AbstractRoleMatcher{
    @Override
    protected void clearAssignment(ScheduleGeneratorContext context) {
        context.getEmployeeCreditDays().clear();
    }

    @Override
    protected boolean specialCheckoutRule() {
        return false;
    }

    @Override
    protected void performSave(ScheduleGeneratorContext context,LocalDate date,Employee employee,Shift shift) {
        context.assignEmployeeToCredit(date,employee,shift);
    }

    @Override
    protected boolean morningHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray) {
        int openHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).openHour();
        int openForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).openHour();

        int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();
        int closeForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).closeHour();

        return (shiftAsArray[openHour] > 0 || shiftAsArray[openForClientsHour] > 0) && (shiftAsArray[closeHour] == 0 || shiftAsArray[closeForClientsHour] == 0);
    }

    @Override
    protected boolean afternoonHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray) {
        int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();
        int closeForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).closeHour();

        return (shiftAsArray[closeHour] > 0 || shiftAsArray[closeForClientsHour] > 0);
    }

    @Override
    protected Comparator<Employee> getSortingRules(ScheduleGeneratorContext context, LocalDate date) {
        return Comparator.comparingInt((Employee empl) -> {
                    List<LocalDate> lastSeven = new ArrayList<>();
                    for (int i = 1; i <= 7; i++){
                        LocalDate subtractedDate = date.minusDays(i);
                        if (context.getEmployeeCreditDays().getOrDefault(empl, Set.of()).contains(subtractedDate)){
                            lastSeven.add(date.minusDays(i));
                        }
                    }

                    return lastSeven.size();
                })
                .thenComparingInt(empl -> context.getVacationDaysCount().getOrDefault(empl,0));
    }

    @Override
    protected List<Employee> getFilteredEmployees(ScheduleGeneratorContext context, LocalDate date) {
        return context.getFinalSchedule().getOrDefault(date, Map.of()).keySet().stream()
                .filter(Employee::isCanOperateCredit)
                .filter(employee -> !context.isEmployeeWorkingInWarehouse(employee, date))
                .filter(employee -> !context.employeeHasProposalDaysOff(employee, date))
                .collect(Collectors.toList());
    }
}