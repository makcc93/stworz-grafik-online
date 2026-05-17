package online.stworzgrafik.StworzGrafik.employee.workNorm.DTO;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSpecialWorkNormDTO(
        @NotBlank @Size(min = 3, max = 100)
        String name,

        @NotNull @DecimalMin("1.00") @DecimalMax("12.00")
        BigDecimal maxDailyHours,

        @NotNull @DecimalMin("1.00") @DecimalMax("60.00")
        BigDecimal weeklyNorm,

        String description
) {}
