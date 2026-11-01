package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Szacowany wpływ przesunięć godzin zmian na liczbę przepracowanych godzin w miesiącu.
 *
 * @param extraHoursTotal       dodatkowe godziny łącznie dla wszystkich pracowników (ujemne = mniej godzin)
 * @param extraHoursPerEmployee dodatkowe godziny na pracownika (extraHoursTotal / totalEmployees) -
 *                              ten sam mianownik co "Wymagane przez draft" (aktywni pracownicy bez magazyniera)
 * @param eligibleEmployees     pracownicy objęci przesunięciami (nie wykluczeni, nie specjalni)
 * @param totalEmployees        aktywni pracownicy bez magazyniera
 * @param rules                 wpływ poszczególnych reguł
 */
public record ShiftHourImpactResponse(
        BigDecimal extraHoursTotal,
        BigDecimal extraHoursPerEmployee,
        int eligibleEmployees,
        int totalEmployees,
        List<ShiftHourRuleImpact> rules
) {
}