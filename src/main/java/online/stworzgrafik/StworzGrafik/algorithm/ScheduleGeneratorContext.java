package online.stworzgrafik.StworzGrafik.algorithm;

import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseStoreHoursDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.store.Store;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Getter
@Builder
public class ScheduleGeneratorContext {
    private final Long storeId;
    private final Integer year;
    private final Integer month;
    private final Schedule schedule;
    private final Store store;
    private final Map<LocalDate, OpenCloseStoreHoursDTO> storeOpenCloseHoursByDate;
    private final List<Employee> storeActiveEmployees;
    private final Map<LocalDate, int[]> uneditedOriginalDateStoreDraft;
    private final LinkedHashMap<LocalDate, int[]> everyDayStoreDemandDraftWorkingOn;
    private final Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate;
    private final Map<Employee, int[]> monthlyEmployeesProposalDayOff;
    private final Map<Employee, int[]> monthlyEmployeesVacation;
    private final Map<Employee, Integer> employeeHours;
    private final Map<Employee, Integer> workingOnWeekendCount;
    private final Map<Employee, Integer> workingDaysCount;
    private final Map<Employee, Integer> vacationDaysCount;
    private final Map<LocalDate, List<Shift>> generatedShiftsByDay;
    private final Map<LocalDate, Employee> employeeReplacingWarehouseman;
    private final List<Shift> allShifts;
    private final Shift defaultVacationShift;
    private final Shift defaultDaysOffShift;
    private final ShiftTypeConfig vacationShiftTypeConfig;
    private final ShiftTypeConfig daysOffShiftTypeConfig;
    private final ShiftTypeConfig proposalShiftTypeConfig;
    private final ShiftTypeConfig standardShiftTypeConfig;
    private final LinkedHashMap<LocalDate,Map<Employee,Shift>> finalSchedule;
    private final List<CreateScheduleMessageDTO> finalScheduleMessages;
    private final boolean storeHasDedicatedWarehouseman;

    public Shift findShiftByArray(int[] array){
        if (array.length != 24){
            throw new IllegalArgumentException("Shift array must equal 24 elements");
        }

        int startHour = 0;
        int endHour = 0;

        for (int i = 0; i < 24; i++){
            if (array[i] != 0){
                startHour = i;
                break;
            }
        }

        for (int i = 23; i >= 0; i--){
            if (array[i] != 0){
                endHour = (i + 1) % 24;
                break;
            }
        }

        return findShiftByHours(LocalTime.of(startHour,0),LocalTime.of(endHour,0));
    }

    public int[] shiftAsArray(Shift shift){
        int startHour = shift.getStartHour().getHour();
        int endHour = shift.getEndHour().getHour();

        if (endHour < startHour){
            return new int[24];
        }

        int[] array = new int[24];


        for (int i = startHour; i < endHour; i++){
            array[i] = 1;
        }

        return array;
    }

    public Shift findShiftByHours(LocalTime startHour, LocalTime endHour){
        for (Shift shift : allShifts){
            if (shift.getStartHour() == startHour && shift.getEndHour() == endHour){
                return shift;
            }
        }
        throw new EntityNotFoundException("Cannot find shift by start hour " + startHour + " and end hour " + endHour);
    }

    public void registerMessageOnSchedule(CreateScheduleMessageDTO dto){
        finalScheduleMessages.add(dto);
    }

    public void updateShiftOnSchedule(LocalDate date, Employee employee, Shift newShift){
        Map<Employee, Shift> dailySchedule = finalSchedule.computeIfAbsent(date, k -> new HashMap<>());
        Shift oldShift = dailySchedule.getOrDefault(employee, defaultDaysOffShift);

        dailySchedule.put(employee,newShift);

        log.info("Modyfikuję zmianę pracownika {} {} z {}-{} na {}-{}",
                employee.getFirstName(),
                employee.getLastName(),
                oldShift.getStartHour(),
                oldShift.getEndHour(),
                newShift.getStartHour(),
                newShift.getEndHour()
                );
    }

    public void deleteShiftFromSchedule(LocalDate date, Employee employee){
        Map<Employee, Shift> dailySchedule = finalSchedule.getOrDefault(date, new HashMap<>());

        if (!dailySchedule.isEmpty()){
            dailySchedule.remove(employee);

            log.info("Usuwam zmianę w dniu {} u pracownika {} {}", date, employee.getFirstName(), employee.getLastName());
        }
    }

    public void registerShiftOnSchedule(LocalDate date, Employee employee, Shift shift){
        log.info("Dzień {} Dopasowuje pracownika {} {} do zmiany {}-{}",
                date,
                employee.getFirstName(),
                employee.getLastName(),
                shift.getStartHour(),
                shift.getEndHour()
        );

        finalSchedule.computeIfAbsent(date, k -> new HashMap<>())
                .put(employee,shift);
    }

    public OpenCloseStoreHoursDTO getStoreOpenCloseHoursByDate(LocalDate date){
        return storeOpenCloseHoursByDate.getOrDefault(date, new OpenCloseStoreHoursDTO(0,0));
    }

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

    public void updateEmployeeDailyProposal(Employee employee, LocalDate date, int[] updatedProposal){
        this.monthlyEmployeesProposalShiftsByDate
                .computeIfAbsent(date, k -> new HashMap<>())
                .put(employee,updatedProposal);
    }

    public void deleteEmployeeDayOffProposal(LocalDate date, Employee employee){
        int day = date.getDayOfMonth();

        int[] employeeMonthlyDayOffProposal = this.monthlyEmployeesProposalDayOff.getOrDefault(employee, new int[31]);

        employeeMonthlyDayOffProposal[day-1] = 0;
    }

    public boolean employeeIsOnDayOff(Employee employee, int day){
        int[] daysOff = this.monthlyEmployeesProposalDayOff.getOrDefault(employee, new int[31]);
        return daysOff[day-1] == 1;
    }

    public boolean employeeHasProposalDaysOff(Employee employee){
        int[] proposalDaysOff = this.monthlyEmployeesProposalDayOff.getOrDefault(employee, new int[31]);

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

    public void updateEmployeeHours(Employee employee, Shift oldShift, Shift newShift){
        int currentEmployeeHoursValue = employeeHours.getOrDefault(employee, 0);

        int oldShiftLengthHours = computeShiftHours(oldShift.getEndHour().getHour(), oldShift.getStartHour().getHour());
        int newShiftLengthHours = computeShiftHours(newShift.getEndHour().getHour(), newShift.getStartHour().getHour());

        int shiftHoursDifference = newShiftLengthHours - oldShiftLengthHours;

        int newValueOfEmployeeHours = currentEmployeeHoursValue + shiftHoursDifference;

        employeeHours.put(employee,newValueOfEmployeeHours);
    }

    public void addEmployeeHours(Employee employee, Shift shift){
        int shiftHours = computeShiftHours(shift.getEndHour().getHour(), shift.getStartHour().getHour());

        int employeeHoursValue = this.employeeHours.getOrDefault(employee, 0);
        int newValueOfEmployeeHours = employeeHoursValue + shiftHours;

        employeeHours.put(employee,newValueOfEmployeeHours);
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
