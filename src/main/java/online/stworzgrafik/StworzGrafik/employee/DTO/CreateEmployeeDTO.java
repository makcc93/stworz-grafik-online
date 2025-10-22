package online.stworzgrafik.StworzGrafik.employee.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployeeDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Employee first name must be between three and fifty chars")
        String firstName,

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 50, message = "Employee last name must be between three and fifty chars")
        String lastName,

        @NotBlank(message = "Sap is required")
        @Size(min = 8, max = 8, message = "Sap number must equals eight chars")
        Long sap,

        @NotNull
        Long storeId,

        @NotNull
        Long positionId
        ) {}
