package online.stworzgrafik.StworzGrafik.employee.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeDTO(
        @Size(min = 3, max = 50, message = "Employee first name must be between three and fifty chars")
        String firstName,

        @Size(min = 3, max = 50, message = "Employee last name must be between three and fifty chars")
        String lastName,

        @Size(min = 8, max = 8, message = "Sap number must equals eight chars")
        Long sap,

        Long storeId,
        Long positionId,
        boolean enable,
        boolean canOperateCheckout,
        boolean canOperateCredit,
        boolean canOpenCloseStore,
        boolean seller,
        boolean manager
) {}
