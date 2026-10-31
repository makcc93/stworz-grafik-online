package online.stworzgrafik.StworzGrafik.draft.template.DTO;

/**
 * Wynik nadpisania draftów dni szablonem.
 *
 * @param created liczba utworzonych draftów (dzień nie miał jeszcze draftu)
 * @param updated liczba nadpisanych istniejących draftów (dzień wolny dostaje same zera)
 * @param skipped dni pominięte: brak draftu i szablon pusty albo dzień wolny
 */
public record DemandTemplateApplyResponse(
        int created,
        int updated,
        int skipped
) {
}