package online.stworzgrafik.StworzGrafik.schedule.message;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
class ScheduleMessageBuilder {

    public ScheduleMessage create(
            Schedule schedule,
            ScheduleMessageType scheduleMessageType,
            ScheduleMessageCode scheduleMessageCode,
            String message,
            @Nullable Employee employee,
            LocalDate messageDate
    ){
        return ScheduleMessage.builder()
                .schedule(schedule)
                .scheduleMessageType(scheduleMessageType)
                .scheduleMessageCode(scheduleMessageCode)
                .message(message)
                .employee(employee)
                .messageDate(messageDate)
                .build();
    }
}
