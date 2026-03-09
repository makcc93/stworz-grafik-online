package online.stworzgrafik.StworzGrafik.store.delivery.DTO;

import online.stworzgrafik.StworzGrafik.store.delivery.DayDeliveryConfig;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;

import java.time.DayOfWeek;
import java.util.Map;

public record UpdateStoreDeliveryDTO(
        Long primaryEmployeeId,
        Map<DayOfWeek, DayDeliveryConfig> deliverySchedule,
        Long updatedByUserId
) {
}
