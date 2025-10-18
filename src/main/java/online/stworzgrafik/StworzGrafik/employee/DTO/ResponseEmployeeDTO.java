package online.stworzgrafik.StworzGrafik.employee.DTO;

import jakarta.validation.constraints.NotNull;

public record ResponseEmployeeDTO(
        @NotNull Long id,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull Long storeId,
        @NotNull Long positionId,
        @NotNull boolean enable
) {
}
