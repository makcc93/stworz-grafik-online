package online.stworzgrafik.StworzGrafik.employee.workNorm.DTO;

import java.math.BigDecimal;

public record ResponseSpecialWorkNormDTO(
        Long id,
        String name,
        BigDecimal maxDailyHours,
        BigDecimal weeklyNorm,
        String description,
        Boolean active
) {}
