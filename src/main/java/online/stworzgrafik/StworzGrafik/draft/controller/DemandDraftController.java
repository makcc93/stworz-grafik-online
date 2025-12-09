package online.stworzgrafik.StworzGrafik.draft.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftService;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DemandDraftController {
    private final DemandDraftService demandDraftService;

    @GetMapping("/:draftId")
    public ResponseEntity<ResponseDemandDraftDTO> findById(@PathVariable @NotNull Long draftId){
        return ResponseEntity.ok().body(demandDraftService.findById(draftId));
    }

    @GetMapping
    public ResponseEntity<List<ResponseDemandDraftDTO>> findAll(){
        return ResponseEntity.ok().body(demandDraftService.findAll());
    }

    @PostMapping
    public ResponseEntity<ResponseDemandDraftDTO> createDraft(
            @RequestParam(required = false) @NotNull Long storeId,
            @RequestBody @NotNull @Valid CreateDemandDraftDTO createDemandDraftDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(demandDraftService.createDemandDraft(storeId,createDemandDraftDTO));
    }
}
