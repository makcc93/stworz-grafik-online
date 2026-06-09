package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleDatabaseSaver {
    private final ScheduleEntityService scheduleEntityService;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ShiftTypeConfigService shiftTypeConfigService;

    void saveScheduleToDatabase(Long storeId,ScheduleGeneratorContext context){
        Schedule schedule = scheduleEntityService.findByStoreIdAndYearAndMonth(storeId, context.getYear(), context.getMonth());

        for (Map.Entry<LocalDate, Map<Employee, Shift>>  entry : context.getFinalSchedule().entrySet()){
            LocalDate date = entry.getKey();
            Map<Employee, Shift> employeeShiftMap = entry.getValue();

            for (Map.Entry<Employee,Shift> dailyEntry : employeeShiftMap.entrySet()){
                Employee employee = dailyEntry.getKey();
                Shift shift = dailyEntry.getValue();

                if (shift == null) shift = context.getDefaultDaysOffShift();

                ShiftTypeConfig shiftTypeConfig = context.resolveShiftTypeConfig(employee, date, shift);

                scheduleDetailsService.addScheduleDetails(
                        storeId,
                        schedule.getId(),
                        new CreateScheduleDetailsDTO(
                            employee.getId(),
                                date,
                                shift.getId(),
                                shiftTypeConfig.getId()
                        )
                );
            }
        }
    }
}
