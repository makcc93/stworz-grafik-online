package online.stworzgrafik.StworzGrafik.shift.controller;

import jakarta.validation.Valid;
import online.stworzgrafik.StworzGrafik.algorithm.ShiftGeneratorAlgorithm;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
class ShiftController {
    private final ShiftService shiftService;
    private final ShiftGeneratorAlgorithm shiftGeneratorAlgorithm;

    public ShiftController(ShiftService shiftService, ShiftGeneratorAlgorithm shiftGeneratorAlgorithm) {
        this.shiftService = shiftService;
        this.shiftGeneratorAlgorithm = shiftGeneratorAlgorithm;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseShiftDTO> getShiftById(@PathVariable Long id){
        return ResponseEntity.ok(shiftService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ResponseShiftDTO>> getAllShifts(){
        return ResponseEntity.ok().body(shiftService.findAll());
    }

    @PostMapping
    public ResponseEntity<ResponseShiftDTO> createShift(@RequestBody @Valid ShiftHoursDTO shiftHoursDTO){
            return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.create(shiftHoursDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteShift(@PathVariable Long id) {
            shiftService.delete(id);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseShiftDTO> updateShift(@PathVariable Long id, @RequestBody @Valid ShiftHoursDTO shiftHoursDTO) {
        return ResponseEntity.ok().body(shiftService.updateShift(id,shiftHoursDTO));
    }

    @GetMapping("/test")
    public List<Shift> testAlgorithm(){
        return shiftGeneratorAlgorithm.generateShiftsWithoutMorningShifts();
    }
}
