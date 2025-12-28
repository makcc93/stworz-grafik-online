package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResponseEmployeeProposalShiftsDTO(
        Long id,
        Long storeId,
        Long employeeId,
        LocalDate date,
        int[] dailyProposalShift,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
