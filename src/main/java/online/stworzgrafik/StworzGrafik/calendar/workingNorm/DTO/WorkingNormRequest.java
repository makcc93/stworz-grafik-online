package online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WorkingNormRequest(
        @NotNull
        @Min(0)
        @Max(744)
        Integer hours
) {
}