package online.stworzgrafik.StworzGrafik.schedule.hours.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SavePeriodHoursCorrectionsRequest(
        @NotNull List<@Valid PeriodHoursCorrectionItemRequest> corrections
) {}
