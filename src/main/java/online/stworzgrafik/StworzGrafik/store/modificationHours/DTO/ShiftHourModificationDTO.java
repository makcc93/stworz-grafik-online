package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ShiftHourModificationDTO(
        @NotNull LocalTime originalHour,
        @NotNull LocalTime modifiedHour
) {}
