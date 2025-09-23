package online.stworzgrafik.StworzGrafik.branch.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.branch.validator.NameValidator;

public record UpdateBranchDTO(
        @NotBlank()
        @Size(min = 3,max = 50,message = "Name length must be between three and fifty characters")
        String name,

        @NotNull()
        Boolean isEnable
) {
    public UpdateBranchDTO{
        name = NameValidator.validate(name);
    }
}
