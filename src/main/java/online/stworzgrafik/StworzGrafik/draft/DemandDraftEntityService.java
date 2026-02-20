package online.stworzgrafik.StworzGrafik.draft;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Validated
public interface DemandDraftEntityService {
    DemandDraft saveEntity(@NotNull DemandDraft demandDraft);
    DemandDraft getEntityById(@NotNull Long id);
    Page<DemandDraft> findEntityFilteredDrafts(@NotNull Long storeId, @Nullable LocalDate startDate, @Nullable LocalDate endDate, Pageable pageable);
}
