package online.stworzgrafik.StworzGrafik.region.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorOLD;

public record CreateRegionDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 3,max = 50,message = "Region name must be between three and fifty chars")
        String name
) {
    public CreateRegionDTO{
        name = NameValidatorOLD.validate(name);
    }
}
