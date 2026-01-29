package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

public record ResponseStoreDetailsDTO(
        Long id,
        Long storeId,
        StoreHoursDTO hours,
        OptimalStaffingDTO staffing
) {}