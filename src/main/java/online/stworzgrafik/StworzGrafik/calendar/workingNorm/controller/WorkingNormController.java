package online.stworzgrafik.StworzGrafik.calendar.workingNorm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormInitializationResponse;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormRequest;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormResponse;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.WorkingNormService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workingNorm")
@RequiredArgsConstructor
public class WorkingNormController {

    private final WorkingNormService workingNormService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{year}")
    ResponseEntity<List<WorkingNormResponse>> findAllByYear(@PathVariable int year) {
        return ResponseEntity.ok(workingNormService.findAllByYear(year));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{year}/{month}")
    ResponseEntity<WorkingNormResponse> findByYearMonth(@PathVariable int year,
                                                        @PathVariable int month) {
        return ResponseEntity.ok(workingNormService.findByYearMonth(year, month));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/initialize")
    ResponseEntity<WorkingNormInitializationResponse> initialize() {
        return ResponseEntity.ok(workingNormService.initializeDefaults());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{year}/{month}")
    ResponseEntity<WorkingNormResponse> update(@PathVariable int year,
                                               @PathVariable int month,
                                               @RequestBody @Valid WorkingNormRequest workingNormRequest) {
        return ResponseEntity.ok(workingNormService.update(year, month, workingNormRequest));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{year}/{month}/reset")
    ResponseEntity<WorkingNormResponse> reset(@PathVariable int year,
                                              @PathVariable int month) {
        return ResponseEntity.ok(workingNormService.resetToDefault(year, month));
    }
}