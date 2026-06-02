package online.stworzgrafik.StworzGrafik.billing;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class BillingPeriodConfigServiceImpl implements BillingPeriodConfigService {
    private final BillingPeriodConfigRepository repository;
    private final BillingPeriodConfigMapper mapper;

    @Override
    @Transactional
    public BillingPeriodConfigResponse create(BillingPeriodConfigRequest request) {
        Optional<BillingPeriodConfig> optionalBillingPeriodConfig = repository.findByStartMonth(request.startMonth());
        if (optionalBillingPeriodConfig.isPresent()) throw new EntityExistsException("Billing period with start month " + request.startMonth() + " already exist");

        BillingPeriodConfig billingPeriodConfig = BillingPeriodConfig.builder()
                .startMonth(request.startMonth())
                .durationMonths(request.durationMonths())
                .build();

        BillingPeriodConfig saved = repository.save(billingPeriodConfig);

        return mapper.toResponse(saved);
    }

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
    public List<Integer> getPeriodMonths(int year, int month) {
        List<Integer> periodMonths = new ArrayList<>();
        BillingPeriodConfig config = repository.findAll().stream()
                .filter(periodConfig -> belongsToPeriod(periodConfig, month))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No billing period config found for month: " + month));

        int startMonth = config.getStartMonth();
        periodMonths.add(startMonth);

        for (int i = 1; i < config.getDurationMonths(); i++){
            periodMonths.add(YearMonth.of(year, startMonth).plusMonths(i).getMonth().getValue());
        }

        return periodMonths;
    }

    @Override
    public Integer getPeriodStartMonth(int month) {
        BillingPeriodConfig config = repository.findAll().stream()
                .filter(periodConfig -> belongsToPeriod(periodConfig, month))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No billing period config found for month: " + month));

        return config.getStartMonth();
    }

    @Override
    public void saveAll(List<BillingPeriodConfig> billingPeriodConfigs) {
        repository.saveAll(billingPeriodConfigs);
    }

    @Override
    public void delete(Long billingPeriodConfigId) {
        BillingPeriodConfig billingPeriodConfig = repository.findById(billingPeriodConfigId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Billing Period by id " + billingPeriodConfigId));

        repository.delete(billingPeriodConfig);
    }

    private boolean belongsToPeriod(BillingPeriodConfig config, int month) {
        for (int i = 0; i < config.getDurationMonths(); i++) {
            int periodMonth = (config.getStartMonth() - 1 + i) % 12 + 1;
            if (periodMonth == month) return true;
        }
        return false;
    }
}