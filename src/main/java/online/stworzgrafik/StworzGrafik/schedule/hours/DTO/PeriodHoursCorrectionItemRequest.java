package online.stworzgrafik.StworzGrafik.schedule.hours.DTO;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PeriodHoursCorrectionItemRequest(
        @NotNull Long employeeId,
        @NotNull BigDecimal correctedHours
) {}
