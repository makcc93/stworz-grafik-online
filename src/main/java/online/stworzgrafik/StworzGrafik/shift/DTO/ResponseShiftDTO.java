package online.stworzgrafik.StworzGrafik.shift.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ResponseShiftDTO(
    @NotNull Long id,
    @NotNull LocalTime startHour,
    @NotNull LocalTime endHour,
    @NotNull Integer length
){}
