package online.stworzgrafik.StworzGrafik.store.modificationHours.controller;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourImpactResponse;
import online.stworzgrafik.StworzGrafik.store.modificationHours.ShiftHourImpactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores/{storeId}/shift-hour-config")
@RequiredArgsConstructor
class ShiftHourImpactController {
    private final ShiftHourImpactService shiftHourImpactService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/impact/{year}/{month}")
    public ResponseEntity<ShiftHourImpactResponse> impact(@PathVariable Long storeId,
                                                          @PathVariable int year,
                                                          @PathVariable int month) {
        return ResponseEntity.ok(shiftHourImpactService.calculate(storeId, year, month));
    }
}