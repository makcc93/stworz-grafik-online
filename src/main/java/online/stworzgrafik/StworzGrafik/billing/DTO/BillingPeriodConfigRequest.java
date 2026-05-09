package online.stworzgrafik.StworzGrafik.billing.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BillingPeriodConfigRequest(
        @NotNull @Min(1) @Max(12) Integer startMonth,
        @NotNull @Min(1) @Max(12) Integer durationMonths
) {}
