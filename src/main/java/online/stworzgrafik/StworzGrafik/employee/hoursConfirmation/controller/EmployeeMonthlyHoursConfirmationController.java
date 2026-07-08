package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.EmployeeHoursConfirmationDTO;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.SaveEmployeeHoursConfirmationRequest;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.EmployeeMonthlyHoursConfirmationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmployeeMonthlyHoursConfirmationController {
    private final EmployeeMonthlyHoursConfirmationService service;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/hoursConfirmation")
    public ResponseEntity<List<EmployeeHoursConfirmationDTO>> getHoursConfirmation(@PathVariable Long storeId,
                                                                                   @RequestParam Integer year,
                                                                                   @RequestParam Integer month) {
        return ResponseEntity.ok(service.getHoursConfirmationForMonth(storeId, year, month));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PutMapping("/stores/{storeId}/hoursConfirmation")
    public ResponseEntity<List<EmployeeHoursConfirmationDTO>> saveHoursConfirmation(@PathVariable Long storeId,
                                                                                    @RequestParam Integer year,
                                                                                    @RequestParam Integer month,
                                                                                    @RequestBody SaveEmployeeHoursConfirmationRequest request) {
        return ResponseEntity.ok(service.saveHoursConfirmation(storeId, year, month, request));
    }
}