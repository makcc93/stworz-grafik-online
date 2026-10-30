package online.stworzgrafik.StworzGrafik.calendar.holidays.DTO;

/**
 * @param initializedYears lata, które nie miały wpisów i zostały uzupełnione
 * @param skippedYears     lata, które już miały wpisy (nie ruszane)
 * @param createdHolidays  liczba dodanych dni wolnych
 */
public record HolidayInitializationResponse(
        int initializedYears,
        int skippedYears,
        int createdHolidays
) {
}