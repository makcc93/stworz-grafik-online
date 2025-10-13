package online.stworzgrafik.StworzGrafik.employee.position.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorOLD;

public record CreatePositionDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Position name must be between three and fifty chars")
        String name,

        String description
) {
    public CreatePositionDTO{
        name = NameValidatorOLD.validateForPosition(name);
    }
}
