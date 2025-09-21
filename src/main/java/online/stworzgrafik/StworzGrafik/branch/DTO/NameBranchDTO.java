package online.stworzgrafik.StworzGrafik.branch.DTO;

import jakarta.validation.constraints.NotBlank;
import online.stworzgrafik.StworzGrafik.branch.validator.NameValidator;

public record NameBranchDTO(
        @NotBlank(message = "Name is required")
        String name
) {
    public NameBranchDTO {
        if (name != null){
            name = NameValidator.validate(name);
        }
    }
}
