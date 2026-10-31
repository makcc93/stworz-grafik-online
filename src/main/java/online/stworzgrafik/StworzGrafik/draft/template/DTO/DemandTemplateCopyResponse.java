package online.stworzgrafik.StworzGrafik.draft.template.DTO;

import java.util.List;

/**
 * @param copied    liczba skopiowanych szablonów (dni tygodnia bez zapisanego szablonu w źródle są pomijane)
 * @param templates komplet 7 szablonów miesiąca docelowego po skopiowaniu
 */
public record DemandTemplateCopyResponse(
        int copied,
        List<DemandTemplateResponse> templates
) {
}