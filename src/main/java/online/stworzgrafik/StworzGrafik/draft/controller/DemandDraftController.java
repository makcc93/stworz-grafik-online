package online.stworzgrafik.StworzGrafik.draft.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Transactional
public class DemandDraftController {
    private final DemandDraftService demandDraftService;

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/drafts/{draftId}")
    public ResponseEntity<ResponseDemandDraftDTO> findById(@PathVariable @NotNull Long storeId,
                                                           @PathVariable @NotNull Long draftId){
        return ResponseEntity.ok().body(demandDraftService.findById(storeId, draftId));
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/drafts")
    public ResponseEntity<List<ResponseDemandDraftDTO>> findByDate(@PathVariable @NotNull Long storeId,
                                                                       @RequestParam(required = false)LocalDate startDate,
                                                                       @RequestParam(required = false) LocalDate endDate){
        return ResponseEntity.ok(demandDraftService.findFilteredDrafts(storeId,startDate,endDate));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/drafts")
    public ResponseEntity<List<ResponseDemandDraftDTO>> findAll(){
        return ResponseEntity.ok().body(demandDraftService.findAll());
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @PostMapping("/stores/{storeId}/drafts")
    public ResponseEntity<ResponseDemandDraftDTO> createDraft(@PathVariable @NotNull Long storeId,
                                                              @RequestBody @NotNull @Valid CreateDemandDraftDTO dto){

        return ResponseEntity.status(HttpStatus.CREATED).body(demandDraftService.createDemandDraft(storeId,dto));
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/drafts/{draftId}")
    public ResponseEntity<ResponseDemandDraftDTO> updateDraft(@PathVariable @NotNull Long storeId,
                                                              @PathVariable @NotNull Long draftId,
                                                              @RequestBody @NotNull @Valid UpdateDemandDraftDTO dto){

        return ResponseEntity.status(HttpStatus.OK).body(demandDraftService.updateDemandDraft(storeId,draftId,dto));
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/drafts/{draftId}")
    public ResponseEntity<ResponseStatus> deleteDraft(@PathVariable @NotNull Long storeId,
                                                      @PathVariable @NotNull Long draftId){
        demandDraftService.deleteDemandDraft(storeId,draftId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
