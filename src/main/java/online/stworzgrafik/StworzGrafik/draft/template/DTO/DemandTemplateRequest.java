package online.stworzgrafik.StworzGrafik.draft.template.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DemandTemplateRequest(
        @NotNull
        @Size(min = 24, max = 24, message = "Demand template must have exactly 24 elements")
        int[] hourlyDemand
) {
}