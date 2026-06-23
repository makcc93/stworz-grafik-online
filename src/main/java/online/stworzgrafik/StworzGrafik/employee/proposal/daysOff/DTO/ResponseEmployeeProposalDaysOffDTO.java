package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO;

import java.time.LocalDateTime;

public record ResponseEmployeeProposalDaysOffDTO(
        Long id,
        Long storeId,
        Long employeeId,
        Integer year,
        Integer month,
        int[] monthlyDaysOff,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdByUserId,
        String createdByLabel,
        Long updatedByUserId,
        String updatedByLabel
) {}
