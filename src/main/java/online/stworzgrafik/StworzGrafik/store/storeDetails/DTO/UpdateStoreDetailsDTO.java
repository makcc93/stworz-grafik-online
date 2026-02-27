package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

public record UpdateStoreDetailsDTO(
        @Valid
        StoreHoursDTO hours,

        @Valid
        OptimalStaffingDTO staffing,

        @Nullable
        Long updatedByUserId
) {}