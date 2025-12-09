package online.stworzgrafik.StworzGrafik.draft.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateDemandDraftDTO(
        @NotNull
        @Min(1900) @Max(2100)
        Integer year,

        @NotNull
        @Min(1) @Max(12)
        Integer month,

        @NotNull
        @Min(1) @Max(31)
        Integer day,

        @NotNull int[] hourlyDemand
) {
}
