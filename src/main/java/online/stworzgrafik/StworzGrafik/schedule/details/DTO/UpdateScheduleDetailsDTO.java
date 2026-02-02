package online.stworzgrafik.StworzGrafik.schedule.details.DTO;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public record UpdateScheduleDetailsDTO(
    @Nullable Long employeeId,
    @Nullable LocalDate date,
    @Nullable Long shiftId,
    @Nullable Long shiftTypeConfigId
    ) {
}
