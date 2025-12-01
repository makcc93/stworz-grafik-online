package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record UpdateEmployeeProposalShiftsDTO(
        Store store,
        Employee employee,
        Integer year,
        Integer month,
        Integer day,
        int[] dailyProposalShift,
        @Nullable LocalDateTime updatedAt
) {
}
