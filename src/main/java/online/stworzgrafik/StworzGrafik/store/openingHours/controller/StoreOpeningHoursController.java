package online.stworzgrafik.StworzGrafik.store.openingHours.controller;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.openingHours.DayHours;
import online.stworzgrafik.StworzGrafik.store.openingHours.StoreOpeningHoursService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class StoreOpeningHoursController {
    private final StoreOpeningHoursService openingHoursService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/opening-hours")
    ResponseEntity<Map<DayOfWeek, DayHours>> getAll(@PathVariable Long storeId) {
        return ResponseEntity.ok(openingHoursService.getHoursForStore(storeId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/opening-hours/{dayOfWeek}")
    ResponseEntity<Void> updateDay(@PathVariable Long storeId,
                                   @PathVariable DayOfWeek dayOfWeek,
                                   @RequestBody DayHours hours) {
        openingHoursService.updateHoursForDayOfWeek(storeId, dayOfWeek, hours);
        return ResponseEntity.noContent().build();
    }
}
