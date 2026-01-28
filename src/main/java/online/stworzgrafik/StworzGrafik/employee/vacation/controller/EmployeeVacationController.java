package online.stworzgrafik.StworzGrafik.employee.vacation.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.EmployeeVacationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmployeeVacationController {
    private final EmployeeVacationService service;
    private final UserAuthorizationService userAuthorizationService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/employees/{employeeId}/vacations/{vacationId}")
    public ResponseEntity<ResponseEmployeeVacationDTO> getVacationById(@PathVariable Long storeId,
                                                                       @PathVariable Long employeeId,
                                                                       @PathVariable Long vacationId){
        return ResponseEntity.ok(service.getById(storeId,employeeId,vacationId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/vacations")
    public ResponseEntity<Page<ResponseEmployeeVacationDTO>> getByCriteria(@PathVariable Long storeId,
                                                                           EmployeeVacationSpecificationDTO dto,
     @PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        return ResponseEntity.ok(service.getByCriteria(storeId,dto,pageable));
    };

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PutMapping("/stores/{storeId}/employees/{employeeId}/vacations")
    public ResponseEntity<ResponseEmployeeVacationDTO> createVacation(@PathVariable Long storeId,
                                                                     @PathVariable Long employeeId,
                                                                     @RequestBody CreateEmployeeVacationDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEmployeeProposalVacation(storeId,employeeId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/employees/{employeeId}/vacations/{vacationId}")
    public ResponseEntity<ResponseEmployeeVacationDTO> updateVacation(@PathVariable Long storeId,
                                                                             @PathVariable Long employeeId,
                                                                             @PathVariable Long vacationId,
                                                                             @RequestBody UpdateEmployeeVacationDTO dto){
        return ResponseEntity.ok(service.updateEmployeeVacation(storeId,employeeId,vacationId,dto));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/employees/{employeeId}/vacations/{vacationId}")
    public ResponseEntity<HttpStatus> deleteProposal(@PathVariable Long storeId,
                                                     @PathVariable Long employeeId,
                                                     @PathVariable Long vacationId){
        service.delete(storeId,employeeId,vacationId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
