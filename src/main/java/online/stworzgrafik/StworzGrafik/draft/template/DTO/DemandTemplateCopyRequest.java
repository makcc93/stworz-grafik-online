package online.stworzgrafik.StworzGrafik.draft.template.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public record DemandTemplateCopyRequest(
        @NotNull Integer targetYear,
        @NotNull @Min(1) @Max(12) Integer targetMonth,
        List<DayOfWeek> dayOfWeeks
) {
}