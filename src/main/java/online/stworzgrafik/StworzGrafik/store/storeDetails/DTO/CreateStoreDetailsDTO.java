package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateStoreDetailsDTO(
        @NotNull(message = "Store ID is required")
        Long storeId,

        @Valid
        OptimalStaffingDTO staffing,

        @Nullable
        Long createdByUserId
) {}
