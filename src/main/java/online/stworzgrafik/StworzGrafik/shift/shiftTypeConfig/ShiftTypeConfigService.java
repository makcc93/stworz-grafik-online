package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public interface ShiftTypeConfigService {
    ShiftTypeConfig findByCode(@NotNull ShiftCode code);
    BigDecimal getDefaultHours(@NotNull ShiftCode code);
    Boolean countsAsWork(@NotNull ShiftCode code);
}
