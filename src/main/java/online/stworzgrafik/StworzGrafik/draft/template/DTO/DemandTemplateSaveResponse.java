package online.stworzgrafik.StworzGrafik.draft.template.DTO;

public record DemandTemplateSaveResponse(
        DemandTemplateResponse template,
        DemandTemplateApplyResponse days
) {
}