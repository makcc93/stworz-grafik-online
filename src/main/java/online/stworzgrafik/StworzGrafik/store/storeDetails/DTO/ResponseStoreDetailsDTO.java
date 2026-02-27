package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import jakarta.annotation.Nullable;

import java.time.LocalDateTime;

public record ResponseStoreDetailsDTO(
        Long id,
        Long storeId,
        StoreHoursDTO hours,
        OptimalStaffingDTO staffing,
        LocalDateTime createdAt,
        @Nullable Long createdByUserId,
        @Nullable LocalDateTime updatedAt,
        @Nullable Long updatedByUserId
) {}