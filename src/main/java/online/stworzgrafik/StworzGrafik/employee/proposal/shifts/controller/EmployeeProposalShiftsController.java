package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Transactional
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmployeeProposalShiftsController {
    private final EmployeeProposalShiftsService service;
    private final UserAuthorizationService userAuthorizationService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/employees/{employeeId}/proposalsShifts/{employeeProposalShiftId}")
    public ResponseEntity<ResponseEmployeeProposalShiftsDTO> getProposalShiftById(@PathVariable Long storeId,
                                                                                  @PathVariable Long employeeId,
                                                                                  @PathVariable Long employeeProposalShiftId){
        return ResponseEntity.ok(service.getById(storeId,employeeId,employeeProposalShiftId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/proposalsShifts")
    public ResponseEntity<List<ResponseEmployeeProposalShiftsDTO>> getByCriteria(@PathVariable Long storeId,
                                                                                 @RequestParam(required = false) LocalDate startDate,
                                                                                 @RequestParam(required = false) LocalDate endDate,
                                                                                 @RequestParam(required = false) Long employeeId){                                                                                ){

        return ResponseEntity.ok(service.getByCriteria(storeId,startDate,endDate,employeeId));
    }
}
