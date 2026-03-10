package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.Builder;
import lombok.Getter;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ScheduleGeneratorContext {
    private final Long storeId;
    private final Integer year;
    private final Integer month;
    private final Schedule schedule;
    private final Store store;
    private final List<Employee> storeActiveEmployees;
    private final Map<LocalDate, int[]> everyDayStoreDemandDraft;
    private final Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate;
    private final Map<Employee, int[]> monthlyEmployeesProposalDayOff;
    private final Map<Employee, int[]> monthlyEmployeesVacation;
    private final Map<Employee, Integer> employeeHours;
    private final Map<Employee, Integer> employeeAmountWorkingOnWeekend;
    private final Map<Employee, Integer> employeeAmountWorkingDays;

    boolean employeeIsOnVacation(Employee employee, int day){
        return monthlyEmployeesVacation.get(employee)[day] == 1;
    }

    void addWorkingInformation(Employee employee, Shift shift, DayOfWeek dayOfWeek){
        addEmployeeHours(employee,shift);
        addEmployeeWorkingDays(employee);
        addEmployeeWorkingOnWeekend(employee,dayOfWeek);
    }

    void addEmployeeHours(Employee employee, Shift shift){
        int shiftHours = computeShiftHours(shift.getEndHour().getHour(), shift.getStartHour().getHour());

        employeeHours.merge(employee,shiftHours,Integer::sum);
    }

    void addEmployeeWorkingOnWeekend(Employee employee, DayOfWeek dayOfWeek){
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY){
            employeeAmountWorkingOnWeekend.merge(employee,1, Integer::sum);
        }
    }

    void addEmployeeWorkingDays(Employee employee){
        employeeAmountWorkingDays.merge(employee,1,Integer::sum);
    }

    boolean employeeHasProposalDayOff(Employee employee, int day){
        return monthlyEmployeesProposalDayOff.get(employee)[day] == 1;
    }

    boolean employeeHasProposalShift(Employee employee, LocalDate date){
        return Arrays.stream(monthlyEmployeesProposalShiftsByDate.get(date).get(employee)).sum() > 0;
    }

    int[] getEmployeeProposalShift(Employee employee, LocalDate date){
        return monthlyEmployeesProposalShiftsByDate.get(date).get(employee);
    }

    private static int computeShiftHours(int shiftEndHour, int shiftsStartHour){
        if (shiftEndHour < shiftsStartHour){
            return (24 - shiftsStartHour) + shiftEndHour;
        }

        return shiftEndHour - shiftsStartHour;
    }
}
