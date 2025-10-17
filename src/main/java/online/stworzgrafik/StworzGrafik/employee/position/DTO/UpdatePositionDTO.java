package online.stworzgrafik.StworzGrafik.employee.position.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePositionDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Position name must be between three and fifty chars")
        String name,

        @Nullable
        String description
) {}
