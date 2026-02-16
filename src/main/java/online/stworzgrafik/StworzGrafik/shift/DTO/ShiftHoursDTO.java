package online.stworzgrafik.StworzGrafik.shift.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ShiftHoursDTO (
        LocalTime startHour,
        LocalTime endHour
){}
