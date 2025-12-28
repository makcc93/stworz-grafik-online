package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmployeeProposalDaysOffController {
    private final EmployeeProposalDaysOffService service;

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/employees/{employeeId}/proposalsDaysOff/{proposalId}")
    public ResponseEntity<ResponseEmployeeProposalDaysOffDTO> getProposalDaysOff(@PathVariable Long storeId,
                                                                                 @PathVariable Long employeeId,
                                                                                 @PathVariable Long proposalId){
        return ResponseEntity.ok(service.findById(storeId,employeeId,proposalId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/stores/proposalsDaysOff")
    public ResponseEntity<List<ResponseEmployeeProposalDaysOffDTO>> getAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @PutMapping("/stores/{storeId}/employees/{employeeId}/proposalsDaysOff")
    public ResponseEntity<ResponseEmployeeProposalDaysOffDTO> createProposal(@PathVariable Long storeId,
                                                                             @PathVariable Long employeeId,
                                                                             @RequestBody CreateEmployeeProposalDaysOffDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEmployeeProposalDaysOff(storeId,employeeId,dto));
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/employees/{employeeId}/proposalsDaysOff/{proposalId}")
    public ResponseEntity<ResponseEmployeeProposalDaysOffDTO> updateProposal(@PathVariable Long storeId,
                                                                             @PathVariable Long employeeId,
                                                                             @PathVariable Long proposalId,
                                                                             @RequestBody UpdateEmployeeProposalDaysOffDTO dto){
        return ResponseEntity.ok(service.updateEmployeeProposalDaysOff(storeId,employeeId,proposalId,dto));
    }

    @PreAuthorize("@userSecurityService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/employees/{employeeId}/proposalsDaysOff/{proposalId}")
    public ResponseEntity<HttpStatus> deleteProposal(@PathVariable Long storeId,
                                                     @PathVariable Long employeeId,
                                                     @PathVariable Long proposalId){
        service.delete(storeId,employeeId,proposalId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
