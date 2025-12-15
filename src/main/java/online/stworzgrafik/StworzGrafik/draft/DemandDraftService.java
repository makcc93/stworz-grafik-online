package online.stworzgrafik.StworzGrafik.draft;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.StoreAccurateDayDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.controller.DraftSearchCriteria;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
public interface DemandDraftService {
    ResponseDemandDraftDTO createDemandDraft(@NotNull Long storeId, @NotNull @Valid CreateDemandDraftDTO dto);
    ResponseDemandDraftDTO updateDemandDraft(@NotNull Long storeId, @NotNull Long draftId, @NotNull @Valid UpdateDemandDraftDTO dto);
    void deleteDemandDraft(@NotNull Long storeId, @NotNull Long draftId);
    List<ResponseDemandDraftDTO> findAll();
    ResponseDemandDraftDTO findById(@NotNull Long storeId, @NotNull Long draftId);
    List<ResponseDemandDraftDTO> findFilteredDrafts(@NotNull Long storeId, @Nullable LocalDate startDate, @Nullable LocalDate endDate);
    boolean exists(@NotNull Long draftId);
    boolean exists(@NotNull @Valid StoreAccurateDayDemandDraftDTO dto);
}
