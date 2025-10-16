package online.stworzgrafik.StworzGrafik.employee.position.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record ResponsePositionDTO(
        @NotNull Long id,
        @NotNull String name,
        @Nullable String description
) {}
