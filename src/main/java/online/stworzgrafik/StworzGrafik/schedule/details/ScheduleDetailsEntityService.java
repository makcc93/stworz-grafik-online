package online.stworzgrafik.StworzGrafik.schedule.details;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ScheduleDetailsEntityService {
    Page<ScheduleDetails> findEntityByCriteria(@NotNull Long storeId,
                                               @NotNull Long scheduleId,
                                               ScheduleDetailsSpecificationDTO dto,
                                               Pageable pageable);
}
