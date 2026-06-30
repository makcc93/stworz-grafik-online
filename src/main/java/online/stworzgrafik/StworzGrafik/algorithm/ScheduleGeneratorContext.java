package online.stworzgrafik.StworzGrafik.algorithm;

import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseHoursForEmployeeIndexDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.store.Store;


import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final Map<Integer, PeriodDateDTO> periodWeek;
    private final Map<LocalDate, OpenCloseHoursForEmployeeIndexDTO> storeOpenCloseHoursForEmployeesByDate;
    private final Map<LocalDate, OpenCloseHoursForEmployeeIndexDTO> storeOpenCloseHoursForClientsByDate;
    private final List<Employee> storeNotSpecialActiveEmployees;
    private final List<Employee> storeAllActiveEmployees;
    private final Map<LocalDate, int[]> uneditedOriginalDateStoreDraft;
    private final LinkedHashMap<LocalDate, int[]> everyDayStoreDemandDraftWorkingOn;
    private final Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate;
    private final Map<Employee, int[]> monthlyEmployeesProposalDayOff;
    private final Map<Employee, int[]> monthlyEmployeesVacation;
    private final Map<Employee, int[]> monthlyEmployeesDelegation;
    private final Map<Employee, BigDecimal> employeeHours;
    private final Map<Employee, Integer> workingOnWeekendCount;
    private final Map<Employee, Integer> workingDaysCount;
    private final Map<Employee, Integer> vacationDaysCount;
    private final Map<LocalDate, List<Shift>> generatedShiftsByDay;
    private final Map<Employee, Set<LocalDate>> employeeWarehouseDays;
    private final Map<Employee, Set<LocalDate>> employeeCreditDays;
    private final Map<Employee, Set<LocalDate>> employeeCheckoutDays;
    private final Map<Employee, Set<LocalDate>> employeeOpenCloseDays;
    private final Map<Employee, Set<LocalDate>> employeeWeeklyRestRequirementDaysOff;
    private final Map<LocalTime, LocalTime> hoursToModify;
    private final List<Employee> employeesToModifyHours;
    private final List<Shift> allShifts;
    private final Shift defaultVacationShift;
    private final Shift defaultDaysOffShift;
    private final Shift defaultDelegationShift;
    private final ShiftTypeConfig vacationShiftTypeConfig;
    private final ShiftTypeConfig daysOffShiftTypeConfig;
    private final ShiftTypeConfig proposalShiftTypeConfig;
    private final ShiftTypeConfig standardShiftTypeConfig;
    private final ShiftTypeConfig delegationShiftTypeConfig;
    private final LinkedHashMap<LocalDate,Map<Employee,Shift>> finalSchedule;
    private final List<CreateScheduleMessageDTO> finalScheduleMessages;
    private final boolean storeHasDedicatedWarehouseman;
    private final boolean storeHasDedicatedCashier;

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
            if (shift.getStartHour().equals(startHour) && shift.getEndHour().equals(endHour)){
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

        log.info("Modyfikuję zmianę pracownika {} {} z {}-{} na {}-{} w dniu {}",
                employee.getFirstName(),
                employee.getLastName(),
                oldShift.getStartHour(),
                oldShift.getEndHour(),
                newShift.getStartHour(),
                newShift.getEndHour(),
                date
                );

        updateEmployeeHours(employee,oldShift,newShift);
    }

    public void deleteShiftFromSchedule(LocalDate date, Employee employee){
        Map<Employee, Shift> dailySchedule = finalSchedule.getOrDefault(date, Map.of());

        if (!dailySchedule.isEmpty()){
            dailySchedule.remove(employee);

            log.info("Usuwam zmianę w dniu {} u pracownika {} {}", date, employee.getFirstName(), employee.getLastName());
        }
    }

    public void registerShiftOnSchedule(LocalDate date, Employee employee, Shift shift, DayOfWeek dayOfWeek){
        log.info("Dzień {} Dopasowuje pracownika {} {} do zmiany {}-{}",
                date,
                employee.getFirstName(),
                employee.getLastName(),
                shift.getStartHour(),
                shift.getEndHour()
        );

        finalSchedule.computeIfAbsent(date, k -> new HashMap<>())
                .put(employee,shift);

        addWorkingInformation(employee,shift,dayOfWeek);

        if (getShiftLength(shift).compareTo(BigDecimal.valueOf(5L)) < 0){
            registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.UNCOMMON_SHIFT_LENGTH,
                            "Pracownik otrzymał bardzo krótką zmianę zgodną z planowaniem, zweryfikuj i podejmij decyzję",
                            employee.getId(),
                            date
                    )
            );
        }
    }

    public OpenCloseHoursForEmployeeIndexDTO getStoreOpenCloseHoursIndexForClientsByDate(LocalDate date){
        return storeOpenCloseHoursForClientsByDate.getOrDefault(date, new OpenCloseHoursForEmployeeIndexDTO(0,0));
    }

    public OpenCloseHoursForEmployeeIndexDTO getStoreOpenCloseHoursIndexForEmployeesByDate(LocalDate date){
        return storeOpenCloseHoursForEmployeesByDate.getOrDefault(date, new OpenCloseHoursForEmployeeIndexDTO(0,0));
    }

    public boolean isEmployeeWorkingInWarehouse(Employee employee, LocalDate date){
        return employeeWarehouseDays.getOrDefault(employee,Set.of()).contains(date);
    }

    public void assignEmployeeToWarehouse(LocalDate date, Employee employee, Shift shift){
        if (!shift.equals(this.defaultDaysOffShift) && !shift.equals(this.defaultVacationShift)) {
            employeeWarehouseDays
                    .computeIfAbsent(employee, k -> new HashSet<>())
                    .add(date);
        }
    }

    public boolean isEmployeeOnRestRequirementDayOff(Employee employee, LocalDate date){
        return employeeWeeklyRestRequirementDaysOff.getOrDefault(employee,Set.of()).contains(date);
    }

    public boolean isEmployeeOnRestRequirementDayOff(Employee employee, LocalDate startDate, LocalDate endDate){
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)){
            if (employeeWeeklyRestRequirementDaysOff.getOrDefault(employee,Set.of()).contains(currentDate)){
                return true;
            }

            currentDate = currentDate.plusDays(1);
        }
        return false;
    }

    public void assignEmployeeToRestRequirementDayOff(Employee employee, LocalDate date){
        employeeWeeklyRestRequirementDaysOff
                .computeIfAbsent(employee, k -> new HashSet<>())
                .add(date);

    }

    public boolean isOpeningOrClosingStore(Employee employee, LocalDate date){
        return employeeOpenCloseDays.getOrDefault(employee,Set.of()).contains(date);
    }

    public void assignEmployeeToOpenClose(LocalDate date, Employee employee, Shift shift){
        if (!shift.equals(this.defaultDaysOffShift) && !shift.equals(this.defaultVacationShift)) {
            employeeOpenCloseDays
                    .computeIfAbsent(employee, k -> new HashSet<>())
                    .add(date);
        }
    }

    public void deleteEmployeeToOpenClose(LocalDate date, Employee employee){
        employeeOpenCloseDays.computeIfAbsent(employee, k -> new HashSet<>())
                .remove(date);
    }

    public boolean isEmployeeWorkingOnCheckout(Employee employee, LocalDate date){
        return employeeCheckoutDays.getOrDefault(employee,Set.of()).contains(date);
    }

    public void assignEmployeeToCheckout(LocalDate date, Employee employee, Shift shift){
        if (!shift.equals(this.defaultDaysOffShift) && !shift.equals(this.defaultVacationShift)) {
            employeeCheckoutDays
                    .computeIfAbsent(employee, k -> new HashSet<>())
                    .add(date);
        }
    }

    public void deleteEmployeeFromCheckout(LocalDate date, Employee employee){
        employeeCheckoutDays.computeIfAbsent(employee, k -> new HashSet<>())
                .remove(date);
    }

    public boolean isEmployeeWorkingOnCredit(Employee employee, LocalDate date){
        return employeeCreditDays.getOrDefault(employee,Set.of()).contains(date);
    }

    public void assignEmployeeToCredit(LocalDate date, Employee employee, Shift shift){
        if (!shift.equals(this.defaultDaysOffShift) && !shift.equals(this.defaultVacationShift)) {
            employeeCreditDays
                    .computeIfAbsent(employee, k -> new HashSet<>())
                    .add(date);
        }
    }

    public void deleteEmployeeFromCredit(LocalDate date, Employee employee){
        employeeCreditDays.computeIfAbsent(employee, k -> new HashSet<>())
                .remove(date);
    }

    public void addShiftsToDay(LocalDate date, List<Shift> shifts){
        generatedShiftsByDay.put(date,shifts);
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

    public boolean employeeHasProposalShift(Employee employee, PeriodDateDTO dto){
        LocalDate startDate = dto.startDate();
        LocalDate endDate = dto.endDate();

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)){
            if (employeeHasProposalShift(employee,currentDate)) return true;

            currentDate = currentDate.plusDays(1);
        }

        return false;
    }

    public boolean employeeIsWorking(Employee employee, LocalDate date){
        Shift shift = finalSchedule.getOrDefault(date, new HashMap<>()).getOrDefault(employee, findShiftByArray(new int[24]));

        return Arrays.stream(shiftAsArray(shift)).sum() > 0 && !shift.equals(defaultVacationShift);
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

    public void addEmployeeDayOffProposal(LocalDate date, Employee employee){
        int day = date.getDayOfMonth();
        int[] employeeMonthlyDayOffProposal = this.monthlyEmployeesProposalDayOff
                .computeIfAbsent(employee, k -> new int[31]);
        employeeMonthlyDayOffProposal[day-1] = 1;
    }

    public boolean employeeHasProposalDaysOff(Employee employee, LocalDate date){
        int[] proposalDaysOff = this.monthlyEmployeesProposalDayOff.getOrDefault(employee, new int[31]);
        int dayIndex = date.getDayOfMonth() - 1;

        return proposalDaysOff[dayIndex] > 0;
    }

    public boolean employeeHasPlannedDelegation(Employee employee){
        int[] delegation = monthlyEmployeesDelegation.getOrDefault(employee,new int[31]);

        return Arrays.stream(delegation).sum() > 0;
    }

    public boolean employeeIsOnDelegation(Employee employee, LocalDate date){
        int[]  delegation = monthlyEmployeesDelegation.getOrDefault(employee,new int[31]);
        int dayOfMonth = date.getDayOfMonth();

        return delegation[dayOfMonth-1] == 1;
    }

    public boolean employeeHasPlannedVacation(Employee employee){
        int[] vacations = monthlyEmployeesVacation.getOrDefault(employee,new int[31]);

        return Arrays.stream(vacations).sum() > 0;
    }

    public boolean employeeIsOnVacation(Employee employee, LocalDate date){
        int[]  vacations = monthlyEmployeesVacation.getOrDefault(employee,new int[31]);
        int dayOfMonth = date.getDayOfMonth();

        return vacations[dayOfMonth-1] == 1;
    }

    public BigDecimal getShiftLength(Shift shift){
        BigDecimal decimalEndHour = BigDecimal.valueOf(shift.getEndHour().getHour())
                .add(BigDecimal.valueOf(shift.getEndHour().getMinute())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));

        BigDecimal decimalStartHour = BigDecimal.valueOf(shift.getStartHour().getHour())
                .add(BigDecimal.valueOf(shift.getStartHour().getMinute())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));

        if (decimalEndHour.compareTo(decimalStartHour) < 0){
            return (BigDecimal.valueOf(24).subtract(decimalStartHour).add(decimalEndHour));
        }

        return decimalEndHour.subtract(decimalStartHour);
    }

    private void addWorkingInformation(Employee employee, Shift shift, DayOfWeek dayOfWeek){
        addEmployeeHours(employee,shift);
        addEmployeeWorkingDays(employee,shift);
        addEmployeeWorkingOnWeekend(employee,shift,dayOfWeek);
    }

    public void addEmployeeVacationDay(Employee employee, Integer numberOfDays){
        vacationDaysCount.merge(employee, numberOfDays, Integer::sum);
    }

    private void updateEmployeeHours(Employee employee, Shift oldShift, Shift newShift){
        BigDecimal currentEmployeeHoursValue = employeeHours.getOrDefault(employee, BigDecimal.ZERO);


        BigDecimal oldShiftLengthHours = getShiftLength(oldShift);
        BigDecimal newShiftLengthHours = getShiftLength(newShift);

        BigDecimal shiftHoursDifference = newShiftLengthHours.subtract(oldShiftLengthHours);

        BigDecimal newValueOfEmployeeHours = currentEmployeeHoursValue.add(shiftHoursDifference);

        employeeHours.put(employee,newValueOfEmployeeHours);
        log.info("AKTUALIZACJA GODZIN pracownika {} {}, poprzednia liczba godzin: {}, nowa: {}",employee.getFirstName(),employee.getLastName(),currentEmployeeHoursValue,newValueOfEmployeeHours);
    }

    private void addEmployeeHours(Employee employee, Shift shift){
        BigDecimal shiftHours = getShiftLength(shift);

        BigDecimal employeeHoursValue = this.employeeHours.getOrDefault(employee, BigDecimal.ZERO);
        BigDecimal newValueOfEmployeeHours = employeeHoursValue.add(shiftHours);

        employeeHours.put(employee,newValueOfEmployeeHours);
    }

    private void addEmployeeWorkingOnWeekend(Employee employee,Shift shift, DayOfWeek dayOfWeek){
        if (Arrays.stream(shiftAsArray(shift)).sum() > 0 && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)){
            workingOnWeekendCount.merge(employee,1, Integer::sum);
        }
    }

    private void addEmployeeWorkingDays(Employee employee, Shift shift){
        if (!shift.equals(this.defaultDaysOffShift)) {
            workingDaysCount.merge(employee, 1, Integer::sum);
        }
    }

    public ShiftTypeConfig resolveShiftTypeConfig(Employee employee, LocalDate date, Shift shift){
        if (shift.equals(defaultDaysOffShift))    return daysOffShiftTypeConfig;
        if (shift.equals(defaultVacationShift))   return vacationShiftTypeConfig;
        if (shift.equals(defaultDelegationShift)) return delegationShiftTypeConfig;
        if (employeeHasProposalShift(employee, date)) return proposalShiftTypeConfig;
        return standardShiftTypeConfig;
    }
}
