package online.stworzgrafik.StworzGrafik.employee.delegation.DTO;

import java.time.LocalDateTime;

public record UpdateEmployeeDelegationDTO(
        Integer year,
        Integer month,
        int[] monthlyDelegation,
        LocalDateTime updatedAt
) {
}
