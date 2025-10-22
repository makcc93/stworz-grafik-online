package online.stworzgrafik.StworzGrafik.employee.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ResponseEmployeeDTO(
        @NotNull Long id,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull Long sap,
        @NotNull Long storeId,
        @NotNull Long positionId,
        @NotNull boolean enable,
        @NotNull boolean canOperateCheckout,
        @NotNull boolean canOperateCredit,
        @NotNull boolean canOpenCloseStore,
        @NotNull boolean seller,
        @NotNull boolean manager,
        @NotNull LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt
) {}
