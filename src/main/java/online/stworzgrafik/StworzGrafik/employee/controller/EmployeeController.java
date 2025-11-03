package online.stworzgrafik.StworzGrafik.employee.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<ResponseEmployeeDTO>> findAll(){
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ResponseEmployeeDTO> findById(@Valid @PathVariable Long employeeId){
        return ResponseEntity.ok(employeeService.findById(employeeId));
    }

    @PostMapping
    public ResponseEntity<ResponseEmployeeDTO> createEmployee (
            @NotNull @PathVariable Long storeId,
            @Valid @RequestBody CreateEmployeeDTO createEmployeeDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(storeId, createEmployeeDTO));
    }

    @PatchMapping("/{employeeId}")
    public ResponseEntity<ResponseEmployeeDTO> updateEmployee(
            @NotNull @PathVariable Long storeId,
            @NotNull @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeDTO updateEmployeeDTO){
        return ResponseEntity.ok(employeeService.updateEmployee(storeId,employeeId,updateEmployeeDTO));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<HttpStatus> deleteEmployee(
            @NotNull @PathVariable Long storeId,
            @NotNull @PathVariable Long employeeId
    ){
        employeeService.deleteEmployee(storeId,employeeId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
