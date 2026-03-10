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

public void generate(ScheduleGeneratorContext context){
        if (!storeDeliveryService.hasDedicatedWarehouseman(context.getStoreId())){
            return;
        }

        StoreDelivery storeDelivery = context.getStore().getDelivery();
        Employee employee = storeDelivery.getPrimaryEmployee();
        ShiftTypeConfig shiftTypeConfig = shiftTypeConfigService.findByCode(ShiftCode.WORK);
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = storeDelivery.getStoreWeeklyDeliverySchedule();
        Map<DayOfWeek, DayDeliveryConfig> deliverySchedule = storeWeeklyDeliverySchedule.getDeliverySchedule();

        for (Map.Entry<DayOfWeek,DayDeliveryConfig> entry : deliverySchedule.entrySet()){
            DayOfWeek dayOfWeek = entry.getKey();
            DayDeliveryConfig dayOfWeekDeliveryConfig = entry.getValue();

            if (!dayOfWeekDeliveryConfig.hasDelivery()){
                continue;
            };

            List<Integer> dayNumbersByDayOfWeek = CalendarCalculation.getDayNumbersByDayOfWeek(context.getYear(), context.getMonth(), dayOfWeek);
            int[] shiftAsArray = dayOfWeekDeliveryConfig.shiftAsArray();
            Shift shift = shiftEntityService.getArrayAsShift(shiftAsArray);

            for (int day : dayNumbersByDayOfWeek){
                LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);

                if (holidayManager.isHoliday(date)){
                    continue;
                }

                if (context.employeeIsOnVacation(employee,day)){
                    context.addEmployeeHours(employee,defaultVacationShift);

                    coverDeliveryByOtherEmployee(context, employee, date,shift,dayOfWeek,shiftTypeConfig);
                    continue;
                }

                if (context.employeeHasProposalDayOff(employee,day)){
                    coverDeliveryByOtherEmployee(context, employee, date,shift,dayOfWeek,shiftTypeConfig);
                    continue;
                }

                if (context.employeeHasProposalShift(employee,date)) {
                    int[] employeeProposalShift = context.getEmployeeProposalShift(employee,date);
                    Shift proposalShift = shiftEntityService.getArrayAsShift(employeeProposalShift);

                    registerWorkOnSchedule(
                            context.getStoreId(),
                            context.getSchedule(),
                            employee,
                            date,
                            proposalShift,
                            shiftTypeConfig);

                    context.addWorkingInformation(employee,proposalShift,dayOfWeek);

                    continue;
                }

                registerWorkOnSchedule(
                        context.getStoreId(),
                        context.getSchedule(),
                        employee,
                        date,
                        shift,
                        shiftTypeConfig);

                context.addWorkingInformation(employee,shift,dayOfWeek);
            }
        }
    }

    private void coverDeliveryByOtherEmployee(ScheduleGeneratorContext context, Employee employee, LocalDate date, Shift shift, DayOfWeek dayOfWeek,ShiftTypeConfig shiftTypeConfig) {
        Optional<Employee> optionalEmployee = context.getStoreActiveEmployees().stream()
                .filter(empl -> empl.isCanOperateDelivery())
                .filter(empl -> !context.employeeIsOnVacation(empl,date.getDayOfMonth()))
                .filter(empl -> !context.employeeHasProposalDayOff(empl,date.getDayOfMonth()))
                .filter(empl -> !context.employeeHasProposalShift(empl,date))
                .filter(empl -> !empl.getId().equals(employee.getId()))
                .sorted(Comparator.comparingInt(context.getEmployeeHours()::get))
                .findFirst();

        if (optionalEmployee.isEmpty()){
            scheduleMessageService.save(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.ERROR,
                            ScheduleMessageCode.NO_EMPLOYEE_TO_COVER_WAREHOUSEMAN,
                            "No employee available to cover warehouseman shift on date " + date,
                            employee.getId(),
                            date
                    )
            );

            return;
        }

        Employee employeeToCoverWarehouseman = optionalEmployee.get();

        registerWorkOnSchedule(
                    context.getStoreId(),
                    context.getSchedule(),
                    employeeToCoverWarehouseman,
                    date,
                    shift,
                    shiftTypeConfig
        );

        context.addWorkingInformation(employeeToCoverWarehouseman,shift,dayOfWeek);

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
}
