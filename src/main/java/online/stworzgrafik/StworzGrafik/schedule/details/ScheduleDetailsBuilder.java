package online.stworzgrafik.StworzGrafik.schedule.details;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
class ScheduleDetailsBuilder {
    public ScheduleDetails createScheduleDetails(
            Schedule schedule,
            Employee employee,
            LocalDate date,
            Shift shift,
            ShiftTypeConfig shiftTypeConfig
    ){
        return ScheduleDetails.builder()
                .schedule(schedule)
                .employee(employee)
                .date(date)
                .shift(shift)
                .shiftTypeConfig(shiftTypeConfig)
                .build();
    }
}
