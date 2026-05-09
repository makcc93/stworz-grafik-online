package online.stworzgrafik.StworzGrafik.billing;

import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigResponse;

import java.time.DayOfWeek;
import java.util.List;

public interface BillingPeriodConfigService {

    List<BillingPeriodConfigResponse> getAll();

    BillingPeriodConfigResponse update(Long id, BillingPeriodConfigRequest request);

    DayOfWeek getDayOfWeekStartingPeriod(int year, int month);

    void saveAll(List<BillingPeriodConfig> billingPeriodConfigs);
}
