package online.stworzgrafik.StworzGrafik.employee.workNorm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.CreateSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.ResponseSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.UpdateSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.SpecialWorkNormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/special-work-norms")
@RequiredArgsConstructor
class SpecialWorkNormController {

    private final SpecialWorkNormService service;

    @GetMapping("/active")
    ResponseEntity<List<ResponseSpecialWorkNormDTO>> getAllActive() {
        return ResponseEntity.ok(service.findAllActive());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    ResponseEntity<List<ResponseSpecialWorkNormDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    ResponseEntity<ResponseSpecialWorkNormDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    ResponseEntity<ResponseSpecialWorkNormDTO> create(@RequestBody @Valid CreateSpecialWorkNormDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{id}")
    ResponseEntity<ResponseSpecialWorkNormDTO> update(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateSpecialWorkNormDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
