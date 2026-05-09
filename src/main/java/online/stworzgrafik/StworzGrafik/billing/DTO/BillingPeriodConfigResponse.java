package online.stworzgrafik.StworzGrafik.billing.DTO;

public record BillingPeriodConfigResponse(
        Long id,
        int startMonth,
        int durationMonths
) {}
