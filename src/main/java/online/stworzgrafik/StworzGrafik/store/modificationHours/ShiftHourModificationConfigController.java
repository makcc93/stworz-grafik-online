package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationConfigResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stores/{storeId}/shift-hour-config")
@RequiredArgsConstructor
class ShiftHourModificationConfigController {

    private final ShiftHourModificationService service;

    @GetMapping("/hours")
    public ResponseEntity<ShiftHourModificationConfigResponse> getHours(@PathVariable Long storeId) {
        return ResponseEntity.ok(service.getHours(storeId));
    }

    @PutMapping("/hours")
    public ResponseEntity<ShiftHourModificationConfigResponse> updateHours(
            @PathVariable Long storeId,
            @RequestBody @Valid ShiftHourMappingRequest request
    ) {
        return ResponseEntity.ok(service.updateHours(storeId, request));
    }

    @GetMapping("/excluded-employees")
    public ResponseEntity<ShiftHourModificationConfigResponse> getExcludedEmployees(@PathVariable Long storeId) {
        return ResponseEntity.ok(service.getExcludedEmployees(storeId));
    }

    @PutMapping("/excluded-employees")
    public ResponseEntity<ShiftHourModificationConfigResponse> updateExcludedEmployees(
            @PathVariable Long storeId,
            @RequestBody @Valid ExcludedEmployeesRequest request
    ) {
        return ResponseEntity.ok(service.updateExcludedEmployees(storeId, request));
    }
}