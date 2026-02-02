package online.stworzgrafik.StworzGrafik.schedule.details.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResponseScheduleDetailsDTO(
        Long id,
        Long scheduleId,
        Long employeeId,
        LocalDate date,
        Long shiftId,
        Long shiftTypeConfigId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
