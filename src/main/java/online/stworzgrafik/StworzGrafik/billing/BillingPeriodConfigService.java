package online.stworzgrafik.StworzGrafik.billing;

import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;

import java.time.DayOfWeek;
import java.util.List;

public interface BillingPeriodConfigService {

    BillingPeriodConfigResponse create(BillingPeriodConfigRequest request);

    List<BillingPeriodConfigResponse> getAll();

    BillingPeriodConfigResponse update(Long id, BillingPeriodConfigRequest request);

    DayOfWeek getDayOfWeekStartingPeriod(int year, int month);

    List<Integer> getPeriodMonths(int year, int month);

    Integer getPeriodStartMonth(int month);

    void saveAll(List<BillingPeriodConfig> billingPeriodConfigs);

    void delete(Long billingPeriodConfigId);
}
