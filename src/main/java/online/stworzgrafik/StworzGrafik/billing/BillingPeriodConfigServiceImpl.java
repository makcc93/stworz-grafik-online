package online.stworzgrafik.StworzGrafik.billing;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class BillingPeriodConfigServiceImpl implements BillingPeriodConfigService {
    private final BillingPeriodConfigRepository repository;
    private final BillingPeriodConfigMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BillingPeriodConfigResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    @Transactional
    public BillingPeriodConfigResponse update(Long id, BillingPeriodConfigRequest request) {
        BillingPeriodConfig config = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BillingPeriodConfig not found: " + id));

        config.setStartMonth(request.startMonth());
        config.setDurationMonths(request.durationMonths());
        repository.save(config);

        log.info("Updated billing period config id={} startMonth={} durationMonths={}",
                id, request.startMonth(), request.durationMonths());

        return mapper.toResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public DayOfWeek getDayOfWeekStartingPeriod(int year, int month) {
        BillingPeriodConfig config = repository.findAll().stream()
                .filter(c -> belongsToPeriod(c, month))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No billing period config found for month: " + month));

        int startYear = (month < config.getStartMonth()) ? year - 1 : year;

        return LocalDate.of(startYear, config.getStartMonth(), 1).getDayOfWeek();
    }

    @Override
    public void saveAll(List<BillingPeriodConfig> billingPeriodConfigs) {
        repository.saveAll(billingPeriodConfigs);
    }

    private boolean belongsToPeriod(BillingPeriodConfig config, int month) {
        for (int i = 0; i < config.getDurationMonths(); i++) {
            int periodMonth = (config.getStartMonth() - 1 + i) % 12 + 1;
            if (periodMonth == month) return true;
        }
        return false;
    }
}