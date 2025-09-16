package online.stworzgrafik.StworzGrafik.branch.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBranchDTO(
        @NotBlank()
        @Size(min = 3,max = 50,message = "Name length must be between three and fifty characters")
        String name,

        @NotBlank
        Boolean isEnable
) {
}
