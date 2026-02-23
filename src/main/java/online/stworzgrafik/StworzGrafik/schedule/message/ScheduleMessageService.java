package online.stworzgrafik.StworzGrafik.schedule.message;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ScheduleMessageService {
    void save(@NotNull Long scheduleId, @Valid CreateScheduleMessageDTO dto);
    void delete(@NotNull Long scheduleId,@NotNull Long scheduleMessageId);
    List<ScheduleMessage> findAll(@NotNull Long scheduleId);
}
