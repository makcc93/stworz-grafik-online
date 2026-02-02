package online.stworzgrafik.StworzGrafik.schedule.details.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateScheduleDetailsDTO(
        @NotNull Long employeeId,
        @NotNull LocalDate date,
        @NotNull Long shiftId,
        @NotNull Long shiftTypeConfigId
) {
}
