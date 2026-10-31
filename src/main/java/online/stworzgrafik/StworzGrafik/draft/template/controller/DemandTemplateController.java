package online.stworzgrafik.StworzGrafik.draft.template.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.template.DTO.*;
import online.stworzgrafik.StworzGrafik.draft.template.DemandTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/draftTemplates")
public class DemandTemplateController {
    private final DemandTemplateService demandTemplateService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<DemandTemplateResponse>> findByMonth(@PathVariable @NotNull Long storeId,
                                                                    @PathVariable int year,
                                                                    @PathVariable int month) {
        return ResponseEntity.ok(demandTemplateService.findByMonth(storeId, year, month));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PutMapping("/{year}/{month}/{dayOfWeek}")
    public ResponseEntity<DemandTemplateSaveResponse> save(@PathVariable @NotNull Long storeId,
                                                           @PathVariable int year,
                                                           @PathVariable int month,
                                                           @PathVariable DayOfWeek dayOfWeek,
                                                           @RequestBody @Valid DemandTemplateRequest request) {
        return ResponseEntity.ok(demandTemplateService.save(storeId, year, month, dayOfWeek, request));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/{year}/{month}/apply")
    public ResponseEntity<DemandTemplateApplyResponse> apply(@PathVariable @NotNull Long storeId,
                                                             @PathVariable int year,
                                                             @PathVariable int month,
                                                             @RequestBody(required = false) DemandTemplateDaysRequest request) {
        return ResponseEntity.ok(demandTemplateService.apply(storeId, year, month, request));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/{year}/{month}/copy")
    public ResponseEntity<DemandTemplateCopyResponse> copy(@PathVariable @NotNull Long storeId,
                                                           @PathVariable int year,
                                                           @PathVariable int month,
                                                           @RequestBody @Valid DemandTemplateCopyRequest request) {
        return ResponseEntity.ok(demandTemplateService.copy(storeId, year, month, request));
    }
}