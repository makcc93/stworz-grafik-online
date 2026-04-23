package online.stworzgrafik.StworzGrafik.store.delivery.DTO;

import online.stworzgrafik.StworzGrafik.store.delivery.DayDeliveryConfig;

import java.time.DayOfWeek;
import java.util.Map;

public record UpdateStoreDeliveryDTO(
        boolean hasDedicatedWarehouseman,
        Long primaryEmployeeId,
        Map<DayOfWeek, DayDeliveryConfig> deliverySchedule,
        Long updatedByUserId
) {
}
