package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.DTO;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;

import java.math.BigDecimal;

public record ShiftTypeConfigRequest(
        @NotNull ShiftCode shiftCode,
        @NotNull String namePl,
        @NotNull BigDecimal defaultHours,
        @NotNull Boolean countsAsWork
        ) {
}
