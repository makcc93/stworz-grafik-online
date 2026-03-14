package online.stworzgrafik.StworzGrafik.shift.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ShiftHoursDTO (
        @Nullable LocalTime startHour,
        @Nullable LocalTime endHour
){}
