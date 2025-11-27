package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
final class ShiftTypeConfigBuilder {
    public ShiftTypeConfig createShiftTypeConfig(
            ShiftCode code,
            String namePl,
            BigDecimal defaultHours,
            Boolean countsAsWork
    ) {
        return ShiftTypeConfig.builder()
                .code(code)
                .namePl(namePl)
                .defaultHours(defaultHours)
                .countsAsWork(countsAsWork).
                build();
    }
}
