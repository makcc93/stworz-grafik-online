package online.stworzgrafik.StworzGrafik.employee.position.controller;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
