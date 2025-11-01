package online.stworzgrafik.StworzGrafik.store.controller;

import jakarta.validation.Valid;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreServiceImpl service;

    public StoreController(StoreServiceImpl service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ResponseStoreDTO>> getAllStores(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseStoreDTO> getStoreById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping()
    public ResponseEntity<ResponseStoreDTO> createStore(@RequestBody @Valid CreateStoreDTO createStoreDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createStore(createStoreDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteById(@PathVariable Long id){
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseStoreDTO> updateStore(@PathVariable Long id, @RequestBody @Valid UpdateStoreDTO updateStoreDTO){
        return ResponseEntity.ok(service.update(id,updateStoreDTO));
    }
}
