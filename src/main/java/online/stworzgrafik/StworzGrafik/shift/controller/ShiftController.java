package online.stworzgrafik.StworzGrafik.shift.controller;

import jakarta.validation.Valid;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftMapper;
import online.stworzgrafik.StworzGrafik.shift.ShiftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
    private final ShiftService shiftService;
    private final ShiftMapper shiftMapper;

    public ShiftController(ShiftService shiftService, ShiftMapper shiftMapper) {
        this.shiftService = shiftService;
        this.shiftMapper = shiftMapper;
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
    public ResponseEntity<ResponseShiftDTO> updateShift(@RequestBody @Valid ShiftHoursDTO shiftHoursDTO,@PathVariable Long id) {
            Shift shift = shiftService.findEntityById(id);
            shift.setStartHour(shiftHoursDTO.startHour());
            shift.setEndHour(shiftHoursDTO.endHour());

            Shift savedEntity = shiftService.saveEntity(shift);

            return ResponseEntity.ok().body(shiftMapper.toShiftDto(savedEntity));
    }
}
