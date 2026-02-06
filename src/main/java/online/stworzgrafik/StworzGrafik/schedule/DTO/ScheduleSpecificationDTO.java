package online.stworzgrafik.StworzGrafik.schedule.DTO;

import java.time.LocalDateTime;

public record ScheduleSpecificationDTO(
        Long scheduleId,
        Integer year,
        Integer month,
        String name,
        LocalDateTime createdAt,
        Long createdByUserId,
        LocalDateTime updatedAt,
        Long updatedByUserId,
        String scheduleStatusName
        ) {
}
