package online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class CheckoutMatcher extends AbstractRoleMatcher{
    @Override
    protected void clearAssignment(ScheduleGeneratorContext context) {
        context.getEmployeeCheckoutDays().clear();
    }

    @Override
    protected boolean specialCheckoutRule() {
        return true;
    }

    @Override
    protected void performSave(ScheduleGeneratorContext context, LocalDate date, Employee employee, Shift shift) {
        context.assignEmployeeToCheckout(date,employee,shift);
    }

    @Override
    protected boolean morningHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray) {
        int openHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).openHour();
        int openForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).openHour();

        return (shiftAsArray[openHour] > 0 || shiftAsArray[openForClientsHour] > 0);
    }

    @Override
    protected boolean afternoonHoursCondition(ScheduleGeneratorContext context, LocalDate date, int[] shiftAsArray) {
        int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();
        int closeForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).closeHour();

        return shiftAsArray[closeHour] > 0 || shiftAsArray[closeForClientsHour] > 0;
    }

    @Override
    protected Comparator<Employee> getSortingRules(ScheduleGeneratorContext context, LocalDate date) {
            return Comparator.comparing(Employee::isCashier, Comparator.reverseOrder())
                    .thenComparing(empl -> !empl.isCanOperateCredit(), Comparator.reverseOrder())
                    .thenComparingInt(empl -> {
                        int checkoutLastSevenDays = 0;
                        for (int i = 1; i <= 7; i++) {
                            if (context.getEmployeeCheckoutDays().getOrDefault(empl, Set.of()).contains(date.minusDays(i))) {
                                checkoutLastSevenDays++;
                            }
                        }
                        return checkoutLastSevenDays;
                    })
                    .thenComparingInt(empl -> context.getVacationDaysCount().getOrDefault(empl, 0));
    }

    @Override
    protected List<Employee> getFilteredEmployees(ScheduleGeneratorContext context, LocalDate date) {
            return context.getFinalSchedule().getOrDefault(date, Map.of()).keySet().stream()
                    .filter(Employee::isCanOperateCheckout)
                    .filter(empl -> context.employeeIsWorking(empl, date))
                    .filter(empl -> !context.isEmployeeWorkingInWarehouse(empl, date))
                    .filter(empl -> !context.isEmployeeWorkingOnCredit(empl, date))
                    .filter(empl -> !context.employeeHasProposalDaysOff(empl, date))
                    .toList();
    }
}