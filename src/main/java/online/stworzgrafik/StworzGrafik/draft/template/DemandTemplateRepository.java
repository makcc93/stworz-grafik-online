package online.stworzgrafik.StworzGrafik.draft.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

interface DemandTemplateRepository extends JpaRepository<DemandTemplate, Long> {
    List<DemandTemplate> findAllByStoreIdAndYearAndMonth(Long storeId, Integer year, Integer month);

    Optional<DemandTemplate> findByStoreIdAndYearAndMonthAndDayOfWeek(Long storeId, Integer year, Integer month, DayOfWeek dayOfWeek);
}