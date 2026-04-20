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
import java.util.*;

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
        Employee warehouseman = storeDelivery.getPrimaryEmployee();
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
            Shift shift = context.findShiftByArray(shiftAsArray);

            for (int day : dayNumbersByDayOfWeek){
                LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);

                if (holidayManager.isHoliday(date) || context.employeeHasProposalShift(warehouseman,date) || shift.equals(context.getDefaultDaysOffShift())){
                    continue;
                }

                if (context.employeeIsOnVacation(warehouseman,day) || context.employeeIsOnDayOff(warehouseman,day)){
                    coverDeliveryByOtherEmployee(context, warehouseman, date,shift,dayOfWeek,shiftTypeConfig);
                    continue;
                }

                context.registerShiftOnSchedule(date,warehouseman,shift,dayOfWeek);
                context.addEmployeeWorkingInWarehouse(date,warehouseman,shift);
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
                .peek(empl -> log.info("W A R E H O U S E, pracownik {} {} ilosc dostaw: {}, suma godzin: {}",
                        empl.getFirstName(),
                        empl.getLastName(),
                        context.getEmployeeInWarehouse().getOrDefault(empl, new ArrayList<>()).size(),
                        context.getEmployeeHours().getOrDefault(empl, 0)
                        ))
                .min(Comparator.comparingInt(
                                empl ->
                                        context.getEmployeeInWarehouse().getOrDefault(empl, new ArrayList<>()).size())
                        .thenComparingInt(
                                empl ->
                                        context.getEmployeeHours().getOrDefault(empl, 0)
                        )
                );



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
        context.registerShiftOnSchedule(date,employeeToCoverWarehouseman,shift,dayOfWeek);
        context.addEmployeeWorkingInWarehouse(date,employeeToCoverWarehouseman,shift);
    }
}