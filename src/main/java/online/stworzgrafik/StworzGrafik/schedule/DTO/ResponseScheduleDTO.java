package online.stworzgrafik.StworzGrafik.schedule.DTO;

import java.time.LocalDateTime;

public record ResponseScheduleDTO (
        Long id,
        Long storeId,
        Integer year,
        Integer month,
        String name,
        LocalDateTime createdAt,
        Long createdByUserId,
        String createdByLabel,
        LocalDateTime updatedAt,
        Long updatedByUserId,
        String updatedByLabel,
        String scheduleStatusName
){}