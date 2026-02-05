package online.stworzgrafik.StworzGrafik.schedule.DTO;

import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

public record CreateScheduleDTO(
        @NotNull Integer year,
        @NotNull Integer month,
        @Nullable String name,
        @NotNull Long createdByUserId,
        @NotNull String scheduleStatusName
) {
}
