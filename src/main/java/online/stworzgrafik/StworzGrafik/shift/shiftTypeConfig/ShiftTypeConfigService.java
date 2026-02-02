package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
public interface ShiftTypeConfigService {
    ShiftTypeConfig findByCode(@NotNull ShiftCode code);
    BigDecimal getDefaultHours(@NotNull ShiftCode code);
    Boolean countsAsWork(@NotNull ShiftCode code);
    ShiftTypeConfig findById(@NotNull Long shiftTypeConfigId);
}
