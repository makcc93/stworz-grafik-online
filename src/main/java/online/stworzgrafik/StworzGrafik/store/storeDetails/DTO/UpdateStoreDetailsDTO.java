package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import jakarta.validation.Valid;

public record UpdateStoreDetailsDTO(
        @Valid
        StoreHoursDTO hours,

        @Valid
        OptimalStaffingDTO staffing
) {}