package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ScheduleEntityService {
    Schedule findEntityById(@NotNull Long scheduleId);
}
