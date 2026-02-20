package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ScheduleEntityService {
    Schedule findEntityById(@NotNull Long scheduleId);
    Page<Schedule> findEntityByCriteria(@NotNull Long storeId, @Nullable ScheduleSpecificationDTO dto, Pageable pageable);
}
