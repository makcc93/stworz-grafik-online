package online.stworzgrafik.StworzGrafik.schedule.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
@Validated
public interface ScheduleMessageService {
    void save(@NotNull Long scheduleId, @Valid CreateScheduleMessageDTO dto);
    void delete(@NotNull Long scheduleId,@NotNull Long scheduleMessageId);
}
