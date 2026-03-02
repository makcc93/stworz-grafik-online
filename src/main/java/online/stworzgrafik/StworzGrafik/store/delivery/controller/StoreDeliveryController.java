package online.stworzgrafik.StworzGrafik.store.delivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class StoreDeliveryController {
    private final StoreDeliveryService storeDeliveryService;

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @GetMapping("/stores/{storeId}/deliveries/{storeDeliveryId}")
    ResponseEntity<ResponseStoreDeliveryDTO> findById(@PathVariable @NotNull Long storeId,
                                      @PathVariable @NotNull Long storeDeliveryId){
        return ResponseEntity.ok(storeDeliveryService.findByStoreId(storeId));
    }

    @PreAuthorize("@userAuthorizationService.hasAccessToStore(#storeId)")
    @PatchMapping("/stores/{storeId}/deliveries")
    ResponseEntity<ResponseStoreDeliveryDTO> updateStoreDelivery(@PathVariable @NotNull Long storeId,
                                                                 @RequestBody @Valid UpdateStoreDeliveryDTO dto){
        return ResponseEntity.ok(storeDeliveryService.update(storeId,dto));
    }
}
