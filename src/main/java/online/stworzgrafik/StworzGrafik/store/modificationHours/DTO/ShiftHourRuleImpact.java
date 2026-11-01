package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import online.stworzgrafik.StworzGrafik.store.modificationHours.ShiftHourScope;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Wpływ jednej reguły (originalHour -> modifiedHour, z zakresem) na wybrany miesiąc.
 *
 * @param shiftsStarting liczba zmian w miesiącu zaczynających się o originalHour, których dotyczy reguła
 *                       (0, gdy zakres to END)
 * @param shiftsEnding   liczba zmian w miesiącu kończących się o originalHour, których dotyczy reguła
 *                       (0, gdy zakres to START)
 * @param extraHours     dodatkowe godziny pracy łącznie wynikające z reguły (ujemne = zmiany krótsze);
 *                       po uwzględnieniu pracowników wykluczonych i specjalnych
 */
public record ShiftHourRuleImpact(
        LocalTime originalHour,
        LocalTime modifiedHour,
        ShiftHourScope scope,
        int shiftsStarting,
        int shiftsEnding,
        BigDecimal extraHours
) {
}