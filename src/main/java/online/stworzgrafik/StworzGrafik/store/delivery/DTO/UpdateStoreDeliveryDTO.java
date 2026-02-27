package online.stworzgrafik.StworzGrafik.store.delivery.DTO;

import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;

public record UpdateStoreDeliveryDTO(
        Long primaryEmployeeId,
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule,
        Long updatedByUserId
) {
}
