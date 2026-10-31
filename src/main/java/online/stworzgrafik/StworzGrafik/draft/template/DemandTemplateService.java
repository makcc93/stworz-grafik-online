package online.stworzgrafik.StworzGrafik.draft.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.draft.template.DTO.*;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.util.List;

@Validated
public interface DemandTemplateService {
    List<DemandTemplateResponse> findByMonth(@NotNull Long storeId, int year, int month);
    DemandTemplateSaveResponse save(@NotNull Long storeId, int year, int month, @NotNull DayOfWeek dayOfWeek,
                                    @NotNull @Valid DemandTemplateRequest request);
    DemandTemplateApplyResponse apply(@NotNull Long storeId, int year, int month, DemandTemplateDaysRequest request);
    DemandTemplateCopyResponse copy(@NotNull Long storeId, int year, int month, @NotNull @Valid DemandTemplateCopyRequest request);
}