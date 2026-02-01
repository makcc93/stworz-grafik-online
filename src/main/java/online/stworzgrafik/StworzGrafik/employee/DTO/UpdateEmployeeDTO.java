package online.stworzgrafik.StworzGrafik.employee.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeDTO(
        @Nullable
        @Size(min = 3, max = 50, message = "Employee first name must be between three and fifty chars")
        String firstName,

        @Nullable
        @Size(min = 3, max = 50, message = "Employee last name must be between three and fifty chars")
        String lastName,

        @Nullable
        @Digits(integer = 8, fraction = 0, message = "Sap number must equals eight chars")
        Long sap,

        Long positionId,
        boolean enable,
        boolean canOperateCheckout,
        boolean canOperateCredit,
        boolean canOpenCloseStore,
        boolean seller,
        boolean manager
) {}
