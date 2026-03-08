package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WarehousemanScheduleGenerator {
    private final EmployeeEntityService employeeEntityService;
    private final StoreEntityService storeEntityService;
    private final StoreDeliveryService storeDeliveryService;
    private final HolidayManager holidayManager;
    private final ScheduleEntityService scheduleEntityService;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ShiftEntityService shiftEntityService;
    private final ShiftTypeConfigService shiftTypeConfigService;

    public void generate(Long storeId, Integer year, Integer month, Schedule schedule,Store store){
        if (!storeDeliveryService.hasDedicatedWarehouseman(storeId)){
            return;
        }

        StoreDelivery storeDelivery = store.getDelivery();
        Employee primaryEmployee = storeDelivery.getPrimaryEmployee();
        ShiftTypeConfig shiftTypeConfig = shiftTypeConfigService.findByCode(ShiftCode.WORK);

        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = storeDelivery.getStoreWeeklyDeliverySchedule();

        if (storeWeeklyDeliverySchedule.isMondayDelivery()){
            int[] mondayShiftAsArray = storeWeeklyDeliverySchedule.getMondayShiftAsArray();
            Shift mondayShift = shiftEntityService.getArrayAsShift(mondayShiftAsArray);

            List<Integer> mondayDayNumbers = CalendarCalculation.getDayNumbersByDayOfWeek(year, month, DayOfWeek.MONDAY);

            for (int day : mondayDayNumbers){
                scheduleDetailsService.addScheduleDetails(
                        storeId,
                        schedule.getId(),
                        new CreateScheduleDetailsDTO(
                                primaryEmployee.getId(),
                                LocalDate.of(year,month,day),
                                mondayShift.getId(),
                                shiftTypeConfig.getId()
                        )
                );
            }
        }
        //todo
        //wyzej zrobilem zapis grafika dla poniedzialkow, trzeba tez dla pozostalych dni tygodnia
        //ale trzeba cos pokminic zeby lepiej to robic niz przez ify (7 ifow dla takiej operacji to chyba nie optymalne rozwaizanie????)
    }

    public void generate1(Long storeId, Integer year, Integer month, Schedule schedule,Store store){
        if (!storeDeliveryService.hasDedicatedWarehouseman(storeId)){
            return;
        }

        StoreDelivery storeDelivery = store.getDelivery();
        Employee primaryEmployee = storeDelivery.getPrimaryEmployee();
        ShiftTypeConfig shiftTypeConfig = shiftTypeConfigService.findByCode(ShiftCode.WORK);

        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule = storeDelivery.getStoreWeeklyDeliverySchedule();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()){
            if (dayOfWeek == DayOfWeek.MONDAY && storeWeeklyDeliverySchedule.isMondayDelivery()){
                int[] mondayShiftAsArray = storeWeeklyDeliverySchedule.getMondayShiftAsArray();
                Shift mondayShift = shiftEntityService.getArrayAsShift(mondayShiftAsArray);

                List<Integer> mondayDayNumbers = CalendarCalculation.getDayNumbersByDayOfWeek(year, month, DayOfWeek.MONDAY);

                for (int day : mondayDayNumbers){
                    scheduleDetailsService.addScheduleDetails(
                            storeId,
                            schedule.getId(),
                            new CreateScheduleDetailsDTO(
                                    primaryEmployee.getId(),
                                    LocalDate.of(year,month,day),
                                    mondayShift.getId(),
                                    shiftTypeConfig.getId()
                            )
                    );
                }
            }
        }
    }


}
