package online.stworzgrafik.StworzGrafik.draft.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateDemandDraftDTO(
        @NotNull LocalDate draftDate,
        @NotNull int[] hourlyDemand
) {
}
