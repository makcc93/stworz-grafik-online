package online.stworzgrafik.StworzGrafik.draft.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateDemandDraftDTO(
        @NotNull
        LocalDate draftDate,

        @NotNull
        @Size(min = 24, max = 24, message = "Daily employee demand draft must have exactly 24 elements")
        int[] hourlyDemand
) {
}
