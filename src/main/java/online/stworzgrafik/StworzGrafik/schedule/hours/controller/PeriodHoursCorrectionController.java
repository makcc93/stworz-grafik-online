package online.stworzgrafik.StworzGrafik.schedule.hours.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.PeriodHoursCorrectionDTO;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.SavePeriodHoursCorrectionsRequest;
import online.stworzgrafik.StworzGrafik.schedule.hours.PeriodHoursCorrectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class PeriodHoursCorrectionController {

    private final PeriodHoursCorrectionService service;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/period-hours-correction")
    ResponseEntity<List<PeriodHoursCorrectionDTO>> getForStore(@PathVariable Long storeId,
                                                               @RequestParam Integer year,
                                                               @RequestParam Integer month) {
        return ResponseEntity.ok(service.getForStore(storeId, year, month));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PutMapping("/stores/{storeId}/period-hours-correction")
    ResponseEntity<Void> saveCorrections(@PathVariable Long storeId,
                                         @RequestParam Integer year,
                                         @RequestParam Integer month,
                                         @RequestBody @Valid SavePeriodHoursCorrectionsRequest request) {
        service.saveCorrections(storeId, year, month, request);
        return ResponseEntity.noContent().build();
    }
}

