package online.stworzgrafik.StworzGrafik.store.openingHours;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.util.Map;

@Validated
public interface StoreOpeningHoursService {
    Map<DayOfWeek, DayHours> getHoursForStore(@NotNull Long storeId);

    DayHours getHoursForDay(@NotNull Long storeId, @NotNull DayOfWeek day);

    void updateHoursForDay(@NotNull Long storeId, @NotNull DayOfWeek day, @NotNull DayHours hours);

    void initializeDefaultHours(@NotNull Store store);
}
