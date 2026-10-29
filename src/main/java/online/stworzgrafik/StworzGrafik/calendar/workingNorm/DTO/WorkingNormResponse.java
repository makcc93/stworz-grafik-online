package online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO;

import java.time.LocalDateTime;

/**
 * @param standardHours wartość domyślna (dni robocze * 8h) - frontend może porównać z hours,
 *                      żeby oznaczyć normę zmienioną ręcznie
 * @param persisted     false = miesiąc nie ma jeszcze rekordu, pokazano wartość domyślną
 */
public record WorkingNormResponse(
        int year,
        int month,
        int hours,
        int standardHours,
        boolean persisted,
        LocalDateTime createdAt,
        Long createdByUserId,
        String createdByLabel,
        LocalDateTime updatedAt,
        Long updatedByUserId,
        String updatedByLabel
) {
}