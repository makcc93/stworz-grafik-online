package online.stworzgrafik.StworzGrafik.branch.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.validator.NameValidator;

public record CreateBranchDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull
        Long regionId
) {
    public CreateBranchDTO {
        if (name != null){
            name = NameValidator.validate(name);
        }
    }
}
