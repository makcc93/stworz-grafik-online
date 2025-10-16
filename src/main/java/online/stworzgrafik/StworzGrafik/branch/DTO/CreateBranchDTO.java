package online.stworzgrafik.StworzGrafik.branch.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBranchDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull
        Long regionId
) {}
