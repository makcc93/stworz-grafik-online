package online.stworzgrafik.StworzGrafik.region.DTO;

import jakarta.validation.constraints.NotNull;

public record ResponseRegionDTO(
        @NotNull Long id,
        @NotNull String name,
        @NotNull boolean enable
) {}
