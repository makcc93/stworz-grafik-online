package online.stworzgrafik.StworzGrafik.store.openingHours;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

interface StoreOpeningHoursRepository extends JpaRepository<StoreOpeningHours, Long> {
    List<StoreOpeningHours> findAllByStoreId(Long storeId);
    Optional<StoreOpeningHours> findByStoreIdAndDayOfWeek(Long storeId, DayOfWeek dayOfWeek);
}
