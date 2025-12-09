package online.stworzgrafik.StworzGrafik.draft.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDemandDraftDTO(
        @NotNull
        @Min(1900)
        @Max(2100)
        Integer year,

        @NotNull
        @Min(1)
        @Max(12)
        Integer month,

        @NotNull
        @Min(1)
        @Max(31)
        Integer day,

        @NotNull
        @Size(min = 24, max = 24, message = "Daily employee demand draft must have exactly 24 elements")
        int[] hourlyDemand
) {
}
