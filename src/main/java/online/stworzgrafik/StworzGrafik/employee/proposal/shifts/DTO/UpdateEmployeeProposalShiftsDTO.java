package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateEmployeeProposalShiftsDTO(
        Store store,
        Employee employee,
        LocalDate date,
        int[] dailyProposalShift,
        @Nullable LocalDateTime updatedAt
) {
}
