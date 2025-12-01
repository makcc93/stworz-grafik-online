package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record ResponseEmployeeProposalShiftsDTO(
        Long id,
        Store store,
        Employee employee,
        Integer year,
        Integer month,
        Integer day,
        int[] dailyProposalShift,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
