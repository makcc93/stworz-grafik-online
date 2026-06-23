package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResponseEmployeeProposalShiftsDTO(
        Long id,
        Long storeId,
        Long employeeId,
        LocalDate date,
        int[] dailyProposalShift,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdByUserId,
        String createdByLabel,
        Long updatedByUserId,
        String updatedByLabel
) {}