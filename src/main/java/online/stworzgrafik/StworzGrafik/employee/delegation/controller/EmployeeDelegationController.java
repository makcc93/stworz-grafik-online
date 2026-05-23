package online.stworzgrafik.StworzGrafik.employee.delegation.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.delegation.EmployeeDelegationService;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.CreateEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.EmployeeDelegationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.ResponseEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.UpdateEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional
public class EmployeeDelegationController {
    private final EmployeeDelegationService service;
    private final UserAuthorizationService userAuthorizationService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/employees/{employeeId}/delegations/{delegationId}")
    public ResponseEntity<ResponseEmployeeDelegationDTO> getDelegationById(@PathVariable Long storeId,
                                                                       @PathVariable Long employeeId,
                                                                       @PathVariable Long delegationId){
        return ResponseEntity.ok(service.getById(storeId,employeeId,delegationId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/delegations")
    public ResponseEntity<Page<ResponseEmployeeDelegationDTO>> getByCriteria(@PathVariable Long storeId,
                                                                           EmployeeDelegationSpecificationDTO dto,
                                                                           @PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        return ResponseEntity.ok(service.getByCriteria(storeId,dto,pageable));
    };

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PutMapping("/stores/{storeId}/employees/{employeeId}/delegations")
    public ResponseEntity<ResponseEmployeeDelegationDTO> createDelegation(@PathVariable Long storeId,
                                                                      @PathVariable Long employeeId,
                                                                      @RequestBody CreateEmployeeDelegationDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEmployeeProposalDelegation(storeId,employeeId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/employees/{employeeId}/delegations/{delegationId}")
    public ResponseEntity<ResponseEmployeeDelegationDTO> updateDelegation(@PathVariable Long storeId,
                                                                      @PathVariable Long employeeId,
                                                                      @PathVariable Long delegationId,
                                                                      @RequestBody UpdateEmployeeDelegationDTO dto){
        return ResponseEntity.ok(service.updateEmployeeDelegation(storeId,employeeId,delegationId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/employees/{employeeId}/delegations/{delegationId}")
    public ResponseEntity<HttpStatus> deleteProposal(@PathVariable Long storeId,
                                                     @PathVariable Long employeeId,
                                                     @PathVariable Long delegationId){
        service.delete(storeId,employeeId,delegationId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
