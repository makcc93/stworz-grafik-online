package online.stworzgrafik.StworzGrafik.algorithm.deliveryCover;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WarehousemanScheduleGenerator {
    private final StoreDeliveryService storeDeliveryService;
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ShiftEntityService shiftEntityService;
    private final ShiftTypeConfigService shiftTypeConfigService;
    private final ScheduleMessageService scheduleMessageService;

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

                if (context.employeeIsOnVacation(employee,day) || context.employeeIsOnDayOff(employee,day)){
                    coverDeliveryByOtherEmployee(context, employee, date,shift,dayOfWeek,shiftTypeConfig);
                    continue;
                }

                if (context.employeeHasProposalShift(employee,date)) {
                    int[] employeeProposalShift = context.employeeProposalShiftAsArray(employee,date);
                    Shift proposalShift = shiftEntityService.getArrayAsShift(employeeProposalShift);

                    registerWorkOnSchedule(context, employee, date, proposalShift);
                    context.addWorkingInformation(employee,proposalShift,dayOfWeek);
                    continue;
                }

                registerWorkOnSchedule(context, employee, date, shift);

                context.addWorkingInformation(employee,shift,dayOfWeek);
            }
        }
    }

    private void coverDeliveryByOtherEmployee(ScheduleGeneratorContext context, Employee employee, LocalDate date, Shift shift, DayOfWeek dayOfWeek,ShiftTypeConfig shiftTypeConfig) {
        Optional<Employee> optionalEmployee = context.getStoreActiveEmployees().stream()
                .filter(Employee::isCanOperateDelivery)
                .filter(empl -> !context.employeeIsOnVacation(empl,date.getDayOfMonth()))
                .filter(empl -> !context.employeeIsOnDayOff(empl,date.getDayOfMonth()))
                .filter(empl -> !context.employeeHasProposalShift(empl,date))
                .filter(empl -> !empl.getId().equals(employee.getId()))
                .sorted(Comparator.comparingInt(context.getEmployeeHours()::get))
                .findFirst();

        if (optionalEmployee.isEmpty()){
            scheduleMessageService.addMessage(
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
        registerWorkOnSchedule(context, employeeToCoverWarehouseman, date, shift);
        context.addWorkingInformation(employeeToCoverWarehouseman,shift,dayOfWeek);
        context.addEmployeeReplacingWarehouseman(date,employeeToCoverWarehouseman);
    }

    private void registerWorkOnSchedule(ScheduleGeneratorContext context, Employee employee, LocalDate date, Shift shift) {
        scheduleDetailsService.addScheduleDetails(
                context.getStoreId(),
                context.getSchedule().getId(),
                new CreateScheduleDetailsDTO(
                        employee.getId(),
                        date,
                        shift.getId(),
                        context.getStandardShiftTypeConfig().getId()
                )
        );
    }
}