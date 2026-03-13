package online.stworzgrafik.StworzGrafik.algorithm;

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
import java.util.Collections;
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
    private final Map<LocalDate, int[]> uneditedOriginalDateStoreDraft;
    private final Map<LocalDate, int[]> everyDayStoreDemandDraftWorkingOn;
    private final Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate;
    private final Map<Employee, int[]> monthlyEmployeesProposalDayOff;
    private final Map<Employee, int[]> monthlyEmployeesVacation;
    private final Map<Employee, Integer> employeeHours;
    private final Map<Employee, Integer> workingOnWeekendCount;
    private final Map<Employee, Integer> workingDaysCount;
    private final Map<Employee, Integer> vacationDaysCount;
    private final Map<LocalDate, List<Shift>> generatedShiftsByDay;
    private final Map<LocalDate, Employee> employeeReplacingWarehouseman;
    private final Shift defaultVacationShift;
    private final Shift defaultDaysOffShift;
    private final ShiftTypeConfig vacationShiftTypeConfig;
    private final ShiftTypeConfig daysOffShiftTypeConfig;
    private final ShiftTypeConfig proposalShiftTypeConfig;
    private final ShiftTypeConfig standardShiftTypeConfig;

    public boolean employeeIsOnReplacementOnWarehouse(LocalDate date, Employee employee){
        return employeeReplacingWarehouseman.get(date) == employee;
    }

    public void addEmployeeReplacingWarehouseman(LocalDate date, Employee employee){
        employeeReplacingWarehouseman.put(date,employee);
    }

    public void addShiftsToDay(LocalDate date, List<Shift> shifts){
        getGeneratedShiftsByDay().put(date,shifts);
    }

    public int[] employeeProposalShiftAsArray(Employee employee, LocalDate date){
        Map<Employee, int[]> shiftsForDate = monthlyEmployeesProposalShiftsByDate.getOrDefault(date, Collections.emptyMap());
        return shiftsForDate.getOrDefault(employee, new int[24]);
    }

    public boolean employeeHasProposalShift(Employee employee, LocalDate date){
        Map<Employee, int[]> shiftsForDate = monthlyEmployeesProposalShiftsByDate.getOrDefault(date, Collections.emptyMap());
        int[] shift = shiftsForDate.getOrDefault(employee, new int[24]);
        return Arrays.stream(shift).sum() > 0;
    }

    public boolean employeeIsOnDayOff(Employee employee, int day){
        int[] daysOff = monthlyEmployeesProposalDayOff.getOrDefault(employee, new int[31]);
        return daysOff[day-1] == 1;
    }

    public boolean employeeHasProposalDaysOff(Employee employee){
        int[] proposalDaysOff = monthlyEmployeesProposalDayOff.getOrDefault(employee, new int[31]);

        return Arrays.stream(proposalDaysOff).sum() > 0;
    }

    public boolean employeeHasPlannedVacation(Employee employee){
        int[] vacations = monthlyEmployeesVacation.getOrDefault(employee,new int[31]);

        return Arrays.stream(vacations).sum() > 0;
    }

    public boolean employeeIsOnVacation(Employee employee, int day){
        int[]  vacations = monthlyEmployeesVacation.getOrDefault(employee,new int[31]);

        return vacations[day-1] == 1;
    }

    public void addWorkingInformation(Employee employee, Shift shift, DayOfWeek dayOfWeek){
        addEmployeeHours(employee,shift);
        addEmployeeWorkingDays(employee);
        addEmployeeWorkingOnWeekend(employee,dayOfWeek);
    }

    public void addEmployeeVacationDay(Employee employee, Integer numberOfDays){
        vacationDaysCount.merge(employee, numberOfDays, Integer::sum);
    }

    public void addEmployeeHours(Employee employee, Shift shift){
        int shiftHours = computeShiftHours(shift.getEndHour().getHour(), shift.getStartHour().getHour());

        employeeHours.merge(employee,shiftHours,Integer::sum);
    }

    void addEmployeeWorkingOnWeekend(Employee employee, DayOfWeek dayOfWeek){
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY){
            workingOnWeekendCount.merge(employee,1, Integer::sum);
        }
    }

    void addEmployeeWorkingDays(Employee employee){
        workingDaysCount.merge(employee,1,Integer::sum);
    }



    private static int computeShiftHours(int shiftEndHour, int shiftsStartHour){
        if (shiftEndHour < shiftsStartHour){
            return (24 - shiftsStartHour) + shiftEndHour;
        }

        return shiftEndHour - shiftsStartHour;
    }
}
