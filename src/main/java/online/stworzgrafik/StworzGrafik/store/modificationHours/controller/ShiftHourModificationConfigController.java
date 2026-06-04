package online.stworzgrafik.StworzGrafik.store.modificationHours.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationConfigResponse;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationCreateRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.ShiftHourModificationService;
import org.springframework.http.HttpStatus;
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

    /**
     * Tworzy nową konfigurację godzin dla sklepu.
     * POPRAWKA: Spring nie obsługuje dwóch @RequestBody w jednym endpoincie.
     * Używamy połączonego DTO ShiftHourModificationCreateRequest.
     */
    @PostMapping
    public ResponseEntity<ShiftHourModificationConfigResponse> create(
            @PathVariable Long storeId,
            @RequestBody @Valid ShiftHourModificationCreateRequest request
    ) {
        ShiftHourMappingRequest hoursRequest = new ShiftHourMappingRequest(request.hours());
        ExcludedEmployeesRequest employeesRequest = new ExcludedEmployeesRequest(request.excludedEmployeeIds());
        return ResponseEntity.ok(service.create(storeId, hoursRequest, employeesRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long storeId) {
        service.delete(storeId);
        return ResponseEntity.noContent().build();
    }
}