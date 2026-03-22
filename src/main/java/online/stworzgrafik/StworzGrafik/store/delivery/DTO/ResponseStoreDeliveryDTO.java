package online.stworzgrafik.StworzGrafik.store.delivery.DTO;

import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;

import java.time.LocalDateTime;

public record ResponseStoreDeliveryDTO(
        Long id,
        Long storeId,
        Long primaryEmployeeId,
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule,
        LocalDateTime createdAt,
        Long createdByUserId,
        LocalDateTime updatedAt,
        Long updatedByUserId
) {
}
