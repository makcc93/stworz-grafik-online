package online.stworzgrafik.StworzGrafik.employee.position.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.validator.NameValidator;

public record UpdatePositionDTO(
        @Size(min = 3, max = 50, message = "Region name must be between three and fifty characters")
        String name,

        String description
) {
    public UpdatePositionDTO{
        name = NameValidator.validateForPosition(name);
    }
}
