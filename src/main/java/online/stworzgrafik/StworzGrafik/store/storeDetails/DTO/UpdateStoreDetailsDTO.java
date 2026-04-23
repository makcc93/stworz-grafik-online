package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

public record UpdateStoreDetailsDTO(
        @Valid
        OptimalStaffingDTO staffing,

        @Nullable
        Long updatedByUserId
) {}