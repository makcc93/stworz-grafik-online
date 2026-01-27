package online.stworzgrafik.StworzGrafik.employee.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.EmployeeSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class EmployeeController {
    private final EmployeeService employeeService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/stores/{storeId}/employees/getAll")
    public ResponseEntity<Page<ResponseEmployeeDTO>> findAll(Pageable pageable){
        return ResponseEntity.ok(employeeService.findAll(pageable));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/employees/{employeeId}")
    public ResponseEntity<ResponseEmployeeDTO> findById(@NotNull @PathVariable Long storeId,
                                                        @NotNull @PathVariable Long employeeId){
        return ResponseEntity.ok(employeeService.findById(storeId, employeeId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/employees")
    public ResponseEntity<Page<ResponseEmployeeDTO>> findByCriteria(@NotNull @PathVariable Long storeId,
                                                              EmployeeSpecificationDTO dto,
                                                              Pageable pageable){
        return ResponseEntity.ok(employeeService.findByCriteria(storeId, dto,pageable));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PostMapping("/stores/{storeId}/employees")
    public ResponseEntity<ResponseEmployeeDTO> createEmployee (@NotNull @PathVariable Long storeId,
                                                               @Valid @RequestBody CreateEmployeeDTO createEmployeeDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(storeId, createEmployeeDTO));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/employees/{employeeId}")
    public ResponseEntity<ResponseEmployeeDTO> updateEmployee(@NotNull @PathVariable Long storeId,
                                                              @NotNull @PathVariable Long employeeId,
                                                              @Valid @RequestBody UpdateEmployeeDTO updateEmployeeDTO){
        return ResponseEntity.ok(employeeService.updateEmployee(storeId,employeeId,updateEmployeeDTO));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}/employees/{employeeId}")
    public ResponseEntity<HttpStatus> deleteEmployee(@NotNull @PathVariable Long storeId,
                                                     @NotNull @PathVariable Long employeeId
    ){
        employeeService.deleteEmployee(storeId,employeeId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
