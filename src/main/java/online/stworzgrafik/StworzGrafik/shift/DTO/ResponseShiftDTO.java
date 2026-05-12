package online.stworzgrafik.StworzGrafik.shift.DTO;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ResponseShiftDTO(
    @NotNull Long id,
    @NotNull LocalTime startHour,
    @NotNull LocalTime endHour,
    @NotNull BigDecimal length
){}
