package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Validated
public interface ScheduleEntityService {
    Schedule findEntityById(@NotNull Long scheduleId);
    Page<Schedule> findEntityByCriteria(@NotNull Long storeId, @Nullable ScheduleSpecificationDTO dto, Pageable pageable);
    Schedule findByStoreIdAndYearAndMonth(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);
    Schedule saveEntity(@NotNull Schedule schedule);
}
