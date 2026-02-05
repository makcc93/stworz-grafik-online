package online.stworzgrafik.StworzGrafik.schedule.DTO;

import java.time.LocalDateTime;

public record UpdateScheduleDTO(
        Long storeId,
        Integer year,
        Integer month,
        String name,
        LocalDateTime updatedAt,
        Long updatedByUserId
) {
}
