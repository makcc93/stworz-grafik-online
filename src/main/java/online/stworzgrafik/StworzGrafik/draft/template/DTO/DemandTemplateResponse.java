package online.stworzgrafik.StworzGrafik.draft.template.DTO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public record DemandTemplateResponse(
        Long id,
        Long storeId,
        int year,
        int month,
        DayOfWeek dayOfWeek,
        int[] hourlyDemand,
        boolean persisted,
        LocalDateTime createdAt,
        Long createdByUserId,
        String createdByLabel,
        LocalDateTime updatedAt,
        Long updatedByUserId,
        String updatedByLabel
) {
}