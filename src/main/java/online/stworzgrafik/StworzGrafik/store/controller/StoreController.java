package online.stworzgrafik.StworzGrafik.store.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreSpecificationDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class StoreController {
    private final StoreService storeService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/stores/getAll")
    public ResponseEntity<List<ResponseStoreDTO>> getAllStores(){
        return ResponseEntity.ok(storeService.findAll());
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ResponseStoreDTO> getStoreById(@PathVariable @NotNull Long storeId){
        return ResponseEntity.ok(storeService.findById(storeId));
    }

    @GetMapping("/stores")
    public ResponseEntity<List<ResponseStoreDTO>> getByCriteria(StoreSpecificationDTO dto){
        return ResponseEntity.ok(storeService.findByCriteria(dto));
    }

    @PostMapping("/stores")
    public ResponseEntity<ResponseStoreDTO> createStore(@RequestBody @Valid CreateStoreDTO createStoreDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(createStoreDTO));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<HttpStatus> deleteById(@PathVariable @NotNull Long storeId){
        storeService.delete(storeId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}")
    public ResponseEntity<ResponseStoreDTO> updateStore(@PathVariable Long storeId,
                                                        @RequestBody @Valid UpdateStoreDTO updateStoreDTO){
        return ResponseEntity.ok(storeService.update(storeId,updateStoreDTO));
    }
}