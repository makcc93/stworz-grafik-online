package online.stworzgrafik.StworzGrafik.schedule.message.DTO;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public record CreateScheduleMessageDTO(
        @NotNull ScheduleMessageType scheduleMessageType,
        @NotNull ScheduleMessageCode scheduleMessageCode,
        @NotNull String message,
        @Nullable Long employeeId,
        @Nullable LocalDate messageDate)
{}
