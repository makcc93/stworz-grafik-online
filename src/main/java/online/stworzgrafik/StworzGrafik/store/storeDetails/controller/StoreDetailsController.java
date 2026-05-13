package online.stworzgrafik.StworzGrafik.store.storeDetails.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.CreateStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.ResponseStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.UpdateStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.StoreDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class StoreDetailsController {
    private final StoreDetailsService storeDetailsService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/details")
    ResponseEntity<ResponseStoreDetailsDTO> getByStoreId(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeDetailsService.findByStoreId(storeId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/details")
    ResponseEntity<ResponseStoreDetailsDTO> update(@PathVariable Long storeId,
                                                   @RequestBody @Valid UpdateStoreDetailsDTO dto) {
        return ResponseEntity.ok(storeDetailsService.update(storeId, dto));
    }
}
