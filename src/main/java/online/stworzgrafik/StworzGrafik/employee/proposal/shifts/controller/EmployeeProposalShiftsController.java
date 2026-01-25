package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.EmployeeProposalShiftsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    @GetMapping("/stores/{storeId}/employees/{employeeId}/proposalShifts/{employeeProposalShiftId}")
    public ResponseEntity<ResponseEmployeeProposalShiftsDTO> getProposalShiftById(@PathVariable Long storeId,
                                                                                  @PathVariable Long employeeId,
                                                                                  @PathVariable Long employeeProposalShiftId){
        return ResponseEntity.ok(service.getById(storeId,employeeId,employeeProposalShiftId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/proposalShifts")
    public ResponseEntity<Page<ResponseEmployeeProposalShiftsDTO>> getByCriteria(@PathVariable Long storeId,
                                                                                 EmployeeProposalShiftsSpecificationDTO dto,
                                                                                 Pageable pageable
                                                                                ){
        return ResponseEntity.ok(service.getByCriteria(storeId, dto,pageable));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PutMapping("/stores/{storeId}/employees/{employeeId}/proposalShifts")
    public ResponseEntity<ResponseEmployeeProposalShiftsDTO> createProposalShift(@PathVariable Long storeId,
                                                                                 @PathVariable Long employeeId,
                                                                                 @RequestBody CreateEmployeeProposalShiftsDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEmployeeProposalShift(storeId,employeeId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/employees/{employeeId}/proposalShifts/{employeeProposalShiftId}")
    public ResponseEntity<ResponseEmployeeProposalShiftsDTO> updateProposalShift(@PathVariable Long storeId,
                                                                                 @PathVariable Long employeeId,
                                                                                 @PathVariable Long employeeProposalShiftId,
                                                                                 @RequestBody UpdateEmployeeProposalShiftsDTO dto){
        return ResponseEntity.ok(service.updateEmployeeProposalShift(storeId,employeeId,employeeProposalShiftId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/employees/{employeeId}/proposalShifts/{employeeProposalShiftId}")
    public ResponseEntity<HttpStatus> deleteProposalShift(@PathVariable Long storeId,
                                                          @PathVariable Long employeeId,
                                                          @PathVariable Long employeeProposalShiftId){
        service.delete(storeId,employeeId,employeeProposalShiftId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}