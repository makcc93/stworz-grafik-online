package online.stworzgrafik.StworzGrafik.draft;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.StoreAccurateDayDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface DemandDraftService {
    ResponseDemandDraftDTO createDemandDraft(@NotNull Long storeId, @NotNull @Valid CreateDemandDraftDTO createDemandDraftDTO);
    ResponseDemandDraftDTO updateDemandDraft(@NotNull Long storeId, @NotNull @Valid UpdateDemandDraftDTO updateDemandDraftDTO);
    void deleteDemandDraft(@NotNull Long storeId);
    List<ResponseDemandDraftDTO> findAll();
    ResponseDemandDraftDTO findById(@NotNull Long id);
    boolean exists(@NotNull Long id);
    boolean exists(@NotNull @Valid StoreAccurateDayDemandDraftDTO storeAccurateDayDemandDraftDTO);


}
