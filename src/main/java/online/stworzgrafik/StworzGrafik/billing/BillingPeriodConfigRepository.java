package online.stworzgrafik.StworzGrafik.billing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface BillingPeriodConfigRepository extends JpaRepository<BillingPeriodConfig, Long> {

    Optional<BillingPeriodConfig> findByStartMonth(int startMonth);

    // znajdź okres dla danego miesiąca — logika filtrowania w serwisie
    List<BillingPeriodConfig> findAll();
}
