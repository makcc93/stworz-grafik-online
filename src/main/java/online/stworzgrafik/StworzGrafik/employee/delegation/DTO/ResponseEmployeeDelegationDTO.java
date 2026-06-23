package online.stworzgrafik.StworzGrafik.employee.delegation.DTO;

import java.time.LocalDateTime;

public record ResponseEmployeeDelegationDTO(
        Long id,
        Long storeId,
        Long employeeId,
        Integer year,
        Integer month,
        int[] monthlyDelegation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdByUserId,
        String createdByLabel,
        Long updatedByUserId,
        String updatedByLabel
) {
}
