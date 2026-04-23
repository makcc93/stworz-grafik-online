package online.stworzgrafik.StworzGrafik.store.openingHours;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class StoreOpeningHoursServiceImpl implements StoreOpeningHoursService{
    private final StoreOpeningHoursRepository repository;

    @Override
    public Map<DayOfWeek, DayHours> getHoursForStore(Long storeId) {
        return repository.findAllByStoreId(storeId).stream()
                .collect(Collectors.toMap(
                        StoreOpeningHours::getDayOfWeek,
                        h -> new DayHours(h.getOpenTime(), h.getCloseTime()),
                        (a, b) -> a,
                        () -> new EnumMap<>(DayOfWeek.class)
                ));
    }

    @Override
    public DayHours getHoursForDay(Long storeId, DayOfWeek day) {
        return repository.findByStoreIdAndDayOfWeek(storeId, day)
                .map(h -> new DayHours(h.getOpenTime(), h.getCloseTime()))
                .orElseThrow(() -> new EntityNotFoundException(
                        "No hours configured for day: " + day));
    }

    @Override
    @Transactional
    public void updateHoursForDay(Long storeId, DayOfWeek day, DayHours hours) {
        StoreOpeningHours entity = repository.findByStoreIdAndDayOfWeek(storeId, day)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No hours configured for day: " + day));

        entity.setOpenTime(hours.open());
        entity.setCloseTime(hours.close());
    }

    @Override
    @Transactional
    public void initializeDefaultHours(Store store) {
        List<StoreOpeningHours> defaultHours = Arrays.stream(DayOfWeek.values())
                .map(day -> StoreOpeningHours.builder()
                        .store(store)
                        .dayOfWeek(day)
                        .openTime(day == DayOfWeek.SUNDAY
                                ? LocalTime.of(10, 0) : LocalTime.of(9, 0))
                        .closeTime(day == DayOfWeek.SUNDAY
                                ? LocalTime.of(19, 0) : LocalTime.of(20, 0))
                        .build())
                .toList();

        repository.saveAll(defaultHours);
    }
}
