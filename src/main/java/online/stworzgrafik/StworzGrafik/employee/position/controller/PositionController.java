package online.stworzgrafik.StworzGrafik.employee.position.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ResponsePositionDTO>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsePositionDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResponsePositionDTO> createPosition(@Valid @RequestBody CreatePositionDTO createPositionDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPosition(createPositionDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponsePositionDTO> updatePosition(@PathVariable Long id, @Valid @RequestBody UpdatePositionDTO updatePositionDTO){
        return ResponseEntity.ok(service.updatePosition(id,updatePositionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePosition(@PathVariable Long id){
        service.deletePosition(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
