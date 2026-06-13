package online.stworzgrafik.StworzGrafik.store.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreSpecificationDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.user.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final UserContext userContext;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/stores/getAll")
    public ResponseEntity<Page<ResponseStoreDTO>> getAllStores(
            @PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(storeService.findAll(pageable));
    }

    @GetMapping("/stores/managed")
    public ResponseEntity<Page<ResponseStoreDTO>> getManagedStores(
            @PageableDefault(size = 100, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        List<Long> managedIds = userContext.getManagedStoreIds();

        if (managedIds.isEmpty()) {
            return ResponseEntity.ok(storeService.findAll(pageable));
        }

        List<ResponseStoreDTO> stores = managedIds.stream()
                .map(id -> {
                    try { return storeService.findById(id); }
                    catch (Exception e) { return null; }
                })
                .filter(s -> s != null)
                .toList();

        return ResponseEntity.ok(new PageImpl<>(stores, pageable, stores.size()));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ResponseStoreDTO> getStoreById(@PathVariable @NotNull Long storeId){
        return ResponseEntity.ok(storeService.findById(storeId));
    }

    @GetMapping("/stores")
    public ResponseEntity<Page<ResponseStoreDTO>> getByCriteria(StoreSpecificationDTO dto,
    @PageableDefault(size = 25, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        return ResponseEntity.ok(storeService.findByCriteria(dto,pageable));
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