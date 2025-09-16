package online.stworzgrafik.StworzGrafik.branch.DTO;

import jakarta.validation.constraints.NotBlank;

public record NameBranchDTO(
        @NotBlank(message = "Name is required")
        String name
) {
    public NameBranchDTO {
        if (name != null){
            name.trim().toUpperCase();
        }
    }
}
