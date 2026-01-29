package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateStoreDetailsDTO(
        @NotNull(message = "Store ID is required")
        Long storeId,

        @Valid
        StoreHoursDTO hours,

        @Valid
        OptimalStaffingDTO staffing
) {}
