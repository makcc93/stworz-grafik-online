package online.stworzgrafik.StworzGrafik.store.delivery.DTO;

import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;
import org.springframework.lang.Nullable;

public record CreateStoreDeliveryDTO(
        @Nullable Long primaryEmployeeId,
        StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule,
        @Nullable Long createdByUserId
) {
}
