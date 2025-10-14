package online.stworzgrafik.StworzGrafik.region.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorOLD;

public record UpdateRegionDTO(
        @NotBlank()
        @Size(min = 3, max = 50, message = "Region name must be between three and fifty characters")
        String name,

        @NotNull
        boolean enable
) {}
