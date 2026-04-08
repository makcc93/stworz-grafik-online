package online.stworzgrafik.StworzGrafik.algorithm.deliveryCover;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.store.delivery.DayDeliveryConfig;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehousemanScheduleGenerator {
    private final HolidayManager holidayManager;
    private final CalendarCalculation calendarCalculation;

public void generate(ScheduleGeneratorContext context){
    log.info("Sprawdzam magazyniera w celu dodania do grafika");

        if (!context.isStoreHasDedicatedWarehouseman()){
            return;
        }

        StoreDelivery storeDelivery = context.getStore().getDelivery();
        Employee employee = storeDelivery.getPrimaryEmployee();
        ShiftTypeConfig shiftTypeConfig = context.getStandardShiftTypeConfig();
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = storeDelivery.getStoreWeeklyDeliverySchedule();
        Map<DayOfWeek, DayDeliveryConfig> deliverySchedule = storeWeeklyDeliverySchedule.getDeliverySchedule();

        for (Map.Entry<DayOfWeek,DayDeliveryConfig> entry : deliverySchedule.entrySet()){
            DayOfWeek dayOfWeek = entry.getKey();
            DayDeliveryConfig dayOfWeekDeliveryConfig = entry.getValue();

            if (!dayOfWeekDeliveryConfig.hasDelivery()){
                continue;
            };

            List<Integer> dayNumbersByDayOfWeek = calendarCalculation.getDayNumbersByDayOfWeek(context.getYear(), context.getMonth(), dayOfWeek);
            int[] shiftAsArray = dayOfWeekDeliveryConfig.shiftAsArray();
            log.info("shift as array {}", shiftAsArray);
            Shift shift = context.findShiftByArray(shiftAsArray);
            log.info("shift: {} - {}", shift.getStartHour().getHour(),shift.getEndHour().getHour());

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
                    Shift proposalShift = context.findShiftByArray(employeeProposalShift);

                    context.registerShiftOnSchedule(date,employee,proposalShift);
                    context.addWorkingInformation(employee,proposalShift,dayOfWeek);
                    continue;
                }

                context.registerShiftOnSchedule(date,employee,shift);
                context.addWorkingInformation(employee,shift,dayOfWeek);
            }
        }
    }

    private void coverDeliveryByOtherEmployee(ScheduleGeneratorContext context, Employee employee, LocalDate date, Shift shift, DayOfWeek dayOfWeek,ShiftTypeConfig shiftTypeConfig) {
        Optional<Employee> optionalEmployee = context.getStoreActiveEmployees().stream()
                .filter(Employee::isCanOperateDelivery)
                .filter(empl -> !context.employeeIsOnVacation(empl, date.getDayOfMonth()))
                .filter(empl -> !context.employeeIsOnDayOff(empl, date.getDayOfMonth()))
                .filter(empl -> !context.employeeHasProposalShift(empl, date))
                .filter(empl -> !empl.getId().equals(employee.getId()))
                .min(Comparator.comparingInt(context.getEmployeeHours()::get));

        if (optionalEmployee.isEmpty()){
            context.registerMessageOnSchedule(
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
        context.registerShiftOnSchedule(date,employeeToCoverWarehouseman,shift);
        context.addWorkingInformation(employeeToCoverWarehouseman,shift,dayOfWeek);
        context.addEmployeeReplacingWarehouseman(date,employeeToCoverWarehouseman);
    }
}