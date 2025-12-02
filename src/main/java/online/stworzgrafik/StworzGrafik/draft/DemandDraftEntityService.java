package online.stworzgrafik.StworzGrafik.draft;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface DemandDraftEntityService {
    DemandDraft saveEntity(@NotNull DemandDraft demandDraft);
    DemandDraft getEntityById(@NotNull Long id);
}
