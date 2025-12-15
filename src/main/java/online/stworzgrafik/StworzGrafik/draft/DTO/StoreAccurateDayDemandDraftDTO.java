package online.stworzgrafik.StworzGrafik.draft.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StoreAccurateDayDemandDraftDTO(
        @NotNull Long storeId,
        @NotNull LocalDate draftDate
) {
}
