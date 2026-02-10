package online.stworzgrafik.StworzGrafik.shift.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ShiftGeneratorAlgorithm;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional
class ShiftController {
    private final ShiftService shiftService;
    private final ShiftGeneratorAlgorithm shiftGeneratorAlgorithm;


    @GetMapping("/shifts/{id}")
    public ResponseEntity<ResponseShiftDTO> getShiftById(@PathVariable Long id){
        return ResponseEntity.ok(shiftService.findById(id));
    }

    @GetMapping("/shifts")
    public ResponseEntity<List<ResponseShiftDTO>> getAllShifts(){
        return ResponseEntity.ok().body(shiftService.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/shifts")
    public ResponseEntity<ResponseShiftDTO> createShift(@RequestBody @Valid ShiftHoursDTO shiftHoursDTO){
            return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.create(shiftHoursDTO));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/shifts/{id}")
    public ResponseEntity<HttpStatus> deleteShift(@PathVariable Long id) {
            shiftService.delete(id);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/shifts/{id}")
    public ResponseEntity<ResponseShiftDTO> updateShift(@PathVariable Long id, @RequestBody @Valid ShiftHoursDTO shiftHoursDTO) {
        return ResponseEntity.ok().body(shiftService.updateShift(id,shiftHoursDTO));
    }

    @GetMapping("/shifts/test")
    public List<Shift> testAlgorithm(){
        return shiftGeneratorAlgorithm.generateShiftsWithoutMorningShifts();
    }
}
