package online.stworzgrafik.StworzGrafik.algorithm;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;
import lombok.Getter;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
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
    private final Shift defaultVacationShift;
    private final ShiftTypeConfig vacationShiftTypeConfig;

    public boolean employeeHasPlannedVacation(Employee employee, Integer year, Integer month){
        int[] vacations = monthlyEmployeesVacation.get(employee);

        return Arrays.stream(vacations).sum() > 0;
    }

    public boolean employeeIsOnVacation(Employee employee, int day){
        return monthlyEmployeesVacation.get(employee)[day] == 1;
    }

    public void addWorkingInformation(Employee employee, Shift shift, DayOfWeek dayOfWeek){
        addEmployeeHours(employee,shift);
        addEmployeeWorkingDays(employee);
        addEmployeeWorkingOnWeekend(employee,dayOfWeek);
    }

    public void addEmployeeHours(Employee employee, Shift shift){
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

    public boolean employeeHasProposalDayOff(Employee employee, int day){
        return monthlyEmployeesProposalDayOff.get(employee)[day] == 1;
    }

    public boolean employeeHasProposalShift(Employee employee, LocalDate date){
        return Arrays.stream(monthlyEmployeesProposalShiftsByDate.get(date).get(employee)).sum() > 0;
    }

    public int[] getEmployeeProposalShift(Employee employee, LocalDate date){
        return monthlyEmployeesProposalShiftsByDate.get(date).get(employee);
    }

    private static int computeShiftHours(int shiftEndHour, int shiftsStartHour){
        if (shiftEndHour < shiftsStartHour){
            return (24 - shiftsStartHour) + shiftEndHour;
        }

        return shiftEndHour - shiftsStartHour;
    }
}
