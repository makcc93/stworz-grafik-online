package online.stworzgrafik.StworzGrafik.calendar.holidays.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @param custom    true = dzień dodany ręcznie, false = święto z jollyday
 * @param persisted false = rok nie jest jeszcze zapisany w bazie, pokazano święta domyślne (jollyday);
 *                  pierwsza zmiana w takim roku najpierw zapisuje wszystkie święta domyślne
 */
public record HolidayResponse(
        LocalDate date,
        String name,
        boolean custom,
        boolean persisted,
        LocalDateTime createdAt,
        String createdByLabel,
        LocalDateTime updatedAt,
        String updatedByLabel
) {
}