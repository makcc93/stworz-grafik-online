package online.stworzgrafik.StworzGrafik.draft;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import org.springframework.validation.annotation.Validated;

@Validated
public interface DemandDraftService {
    ResponseDemandDraftDTO createDemandDraft(@NotNull @Valid CreateDemandDraftDTO createDemandDraftDTO);
}
