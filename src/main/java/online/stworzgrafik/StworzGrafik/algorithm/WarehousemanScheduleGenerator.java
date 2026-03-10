package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.delivery.DayDeliveryConfig;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class WarehousemanScheduleGenerator {
    private final StoreDeliveryService storeDeliveryService;
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ShiftEntityService shiftEntityService;
    private final ShiftTypeConfigService shiftTypeConfigService;
    private final ScheduleMessageService scheduleMessageService;
    private final Shift defaultVacationShift = Shift.builder().startHour(LocalTime.of(12,0)).endHour(LocalTime.of(20,0)).build();

    public void generate(Long storeId, Integer year, Integer month, Schedule schedule,Store store,
                         Map<Employee, Integer> employeeAmountWorkingAndVacationHours,
                         Map<Employee, Integer> employeeAmountWorkingDays,
                         Map<Employee, Integer> employeeAmountWorkingOnWeekend,
                         List<Employee> storeActiveEmployees,
                         Map<Employee, int[]> monthlyEmployeesVacationByMonth,
                         Map<Employee, int[]> monthlyEmployeesProposalDayOffByMonth,
                         Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate){
        if (!storeDeliveryService.hasDedicatedWarehouseman(storeId)){
            return;
        }

        StoreDelivery storeDelivery = store.getDelivery();
        Employee warehouseman = storeDelivery.getPrimaryEmployee();
        ShiftTypeConfig shiftTypeConfig = shiftTypeConfigService.findByCode(ShiftCode.WORK);
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = storeDelivery.getStoreWeeklyDeliverySchedule();
        Map<DayOfWeek, DayDeliveryConfig> deliverySchedule = storeWeeklyDeliverySchedule.getDeliverySchedule();

        for (Map.Entry<DayOfWeek,DayDeliveryConfig> entry : deliverySchedule.entrySet()){
            DayOfWeek dayOfWeek = entry.getKey();
            DayDeliveryConfig dayOfWeekDeliveryConfig = entry.getValue();

            if (!dayOfWeekDeliveryConfig.hasDelivery()){
                continue;
            };

            List<Integer> dayNumbersByDayOfWeek = CalendarCalculation.getDayNumbersByDayOfWeek(year, month, dayOfWeek);
            int[] shiftAsArray = dayOfWeekDeliveryConfig.shiftAsArray();
            Shift shift = shiftEntityService.getArrayAsShift(shiftAsArray);

            for (int day : dayNumbersByDayOfWeek){
                LocalDate date = LocalDate.of(year, month, day);

                if (holidayManager.isHoliday(date)){
                    continue;
                }

                //*** VACATION ***
                int[] warehousemanVacation = monthlyEmployeesVacationByMonth.get(warehouseman);
                if (warehousemanVacation[day] == 1) {
                    addEmployeeWorkingHours(employeeAmountWorkingAndVacationHours,warehouseman, defaultVacationShift);

                    coverDeliveryByOtherEmployee(
                            storeId,
                            schedule,
                            employeeAmountWorkingAndVacationHours, employeeAmountWorkingDays,
                            employeeAmountWorkingOnWeekend,
                            storeActiveEmployees,
                            monthlyEmployeesVacationByMonth,
                            monthlyEmployeesProposalDayOffByMonth,
                            monthlyEmployeesProposalShiftsByDate,
                            dayOfWeek,
                            day,
                            warehouseman,
                            date, shift,
                            shiftTypeConfig
                    );
                    continue;
                }
                //VACATION ^

                //*** PROPOSAL DAY OFF ***
                int[] warehousemanProposalDayOff = monthlyEmployeesProposalDayOffByMonth.get(warehouseman);
                if (warehousemanProposalDayOff[day] == 1){
                    coverDeliveryByOtherEmployee(
                            storeId,
                            schedule,
                            employeeAmountWorkingAndVacationHours, employeeAmountWorkingDays,
                            employeeAmountWorkingOnWeekend,
                            storeActiveEmployees,
                            monthlyEmployeesVacationByMonth,
                            monthlyEmployeesProposalDayOffByMonth,
                            monthlyEmployeesProposalShiftsByDate,
                            dayOfWeek,
                            day,
                            warehouseman,
                            date, shift,
                            shiftTypeConfig
                    );
                    continue;
                }
                //PROPOSAL DAY OFF ^

                //*** PROPOSAL SHIFT ***
                int[] warehousemanProposalShift = monthlyEmployeesProposalShiftsByDate.get(date).get(warehouseman);
                if (Arrays.stream(warehousemanProposalShift).sum() > 0) {
                    Shift proposalShift = shiftEntityService.getArrayAsShift(warehousemanProposalShift);
                    registerWorkOnSchedule(
                            storeId,
                            schedule,
                            warehouseman,
                            date,
                            proposalShift,
                            shiftTypeConfig);

                    addWorkingInformation(employeeAmountWorkingAndVacationHours, employeeAmountWorkingDays, employeeAmountWorkingOnWeekend, warehouseman, proposalShift, dayOfWeek);

                    continue;
                }
                //PROPOSAL SHIFT ^

                registerWorkOnSchedule(
                        storeId,
                        schedule,
                        warehouseman,
                        date,
                        shift,
                        shiftTypeConfig);

                addWorkingInformation(employeeAmountWorkingAndVacationHours,employeeAmountWorkingDays,employeeAmountWorkingOnWeekend,warehouseman,shift,dayOfWeek);
            }
        }
    }

    private void coverDeliveryByOtherEmployee(Long storeId, Schedule schedule, Map<Employee, Integer> employeeAmountWorkingAndVacationHours, Map<Employee, Integer> employeeAmountWorkingDays, Map<Employee, Integer> employeeAmountWorkingOnWeekend, List<Employee> storeActiveEmployees, Map<Employee, int[]> monthlyEmployeesVacationByMonth, Map<Employee, int[]> monthlyEmployeesProposalDayOffByMonth,Map<LocalDate, Map<Employee, int[]>> monthlyEmployeesProposalShiftsByDate, DayOfWeek dayOfWeek, int day, Employee warehouseman, LocalDate date, Shift shift, ShiftTypeConfig shiftTypeConfig) {
        Optional<Employee> optionalEmployee = storeActiveEmployees.stream()
                .filter(empl -> empl.isCanOperateDelivery())
                .filter(empl -> monthlyEmployeesVacationByMonth.get(empl)[day] == 0)
                .filter(empl -> monthlyEmployeesProposalDayOffByMonth.get(empl)[day] == 0)
                .filter(empl -> Arrays.stream(monthlyEmployeesProposalShiftsByDate.get(date).get(empl)).sum() == 0)
                .filter(empl -> !empl.getId().equals(warehouseman.getId()))
                .sorted(Comparator.comparingInt(employeeAmountWorkingAndVacationHours::get))
                .findFirst();

        if (optionalEmployee.isEmpty()){
            scheduleMessageService.save(
                    schedule.getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.ERROR,
                            ScheduleMessageCode.NO_EMPLOYEE_TO_COVER_WAREHOUSEMAN,
                            "No employee available to cover warehouseman shift on date " + date,
                            warehouseman.getId(),
                            date
                    )
            );

            return;
        }

        Employee employeeToCoverWarehouseman = optionalEmployee.get();

        registerWorkOnSchedule(
                    storeId,
                    schedule,
                    employeeToCoverWarehouseman,
                    date,
                    shift,
                    shiftTypeConfig
        );

        addWorkingInformation(employeeAmountWorkingAndVacationHours,employeeAmountWorkingDays,employeeAmountWorkingOnWeekend,employeeToCoverWarehouseman,shift,dayOfWeek);
    }

    private void registerWorkOnSchedule(Long storeId, Schedule schedule, Employee employeeCoveringWarehouseman, LocalDate date, Shift shift, ShiftTypeConfig shiftTypeConfig) {
        scheduleDetailsService.addScheduleDetails(
                storeId,
                schedule.getId(),
                new CreateScheduleDetailsDTO(
                        employeeCoveringWarehouseman.getId(),
                        date,
                        shift.getId(),
                        shiftTypeConfig.getId()
                )
        );
    }

    private void addWorkingInformation(Map<Employee, Integer> employeeAmountWorkingAndVacationHours,
                                       Map<Employee, Integer> employeeAmountWorkingDays,
                                       Map<Employee, Integer> employeeAmountWorkingOnWeekend,
                                       Employee employee,
                                       Shift shift,
                                       DayOfWeek dayOfWeek){
        addEmployeeWorkingOnWeekend(employeeAmountWorkingOnWeekend,employee,dayOfWeek);
        addEmployeeWorkingDays(employeeAmountWorkingDays,employee);
        addEmployeeWorkingHours(employeeAmountWorkingAndVacationHours,employee,shift);

    };
    
    private static void addEmployeeWorkingOnWeekend(Map<Employee, Integer> employeeAmountWorkingOnWeekend, Employee employee, DayOfWeek dayOfWeek){
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            Integer amountOfWorkingOnWeekend = employeeAmountWorkingOnWeekend.getOrDefault(employee, 0);
            employeeAmountWorkingOnWeekend.put(
                    employee,
                    (amountOfWorkingOnWeekend + 1)
            );
        }
    }

    private static void addEmployeeWorkingDays(Map<Employee, Integer> employeeAmountWorkingDays, Employee employee) {
        Integer amountWorkingDays = employeeAmountWorkingDays.getOrDefault(employee,0);
        employeeAmountWorkingDays.put(
                employee,
                (amountWorkingDays + 1)
        );
    }

    private static void addEmployeeWorkingHours(Map<Employee, Integer> employeeAmountWorkingAndVacationHours, Employee employee, Shift shift) {
        Integer amountWorkingHours = employeeAmountWorkingAndVacationHours.getOrDefault(employee,0);
        int shiftHours = computeShiftHours(shift.getEndHour().getHour(), shift.getStartHour().getHour());

        employeeAmountWorkingAndVacationHours.put(
                employee,
                (amountWorkingHours + shiftHours)
                );
    }

    private static int computeShiftHours(int shiftEndHour, int shiftsStartHour){
        if (shiftEndHour < shiftsStartHour){
            return (24- shiftsStartHour) + shiftEndHour;
        }

        return shiftEndHour - shiftsStartHour;
    }
}
