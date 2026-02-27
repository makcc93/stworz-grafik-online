package online.stworzgrafik.StworzGrafik.store.delivery.DTO;

import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;

public record ResponseStoreDeliveryDTO(
        Long id,
        Long storeId,
        Long primaryEmployeeId,
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule,
        Boolean createdAt,
        Long createdByUserId,
        Boolean updatedAt,
        Long updatedByUserId
) {
}
