package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.modificationHours.ShiftHourScope;

import java.time.LocalTime;

public record ShiftHourModificationDTO(
        @NotNull LocalTime originalHour,
        @NotNull LocalTime modifiedHour,
        ShiftHourScope scope
) {
    public ShiftHourModificationDTO {
        if (scope == null) {
            scope = ShiftHourScope.BOTH;
        }
    }
}