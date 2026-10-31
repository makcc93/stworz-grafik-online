package online.stworzgrafik.StworzGrafik.draft.template.DTO;

import java.time.DayOfWeek;
import java.util.List;

public record DemandTemplateDaysRequest(
        List<DayOfWeek> dayOfWeeks
) {
}