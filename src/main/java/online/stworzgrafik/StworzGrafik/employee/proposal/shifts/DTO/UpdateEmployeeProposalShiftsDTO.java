package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateEmployeeProposalShiftsDTO(
        @NotNull
        LocalDate date,

        @Size(min = 24,max = 24)
        int[] dailyProposalShift,

        @Nullable
        LocalDateTime updatedAt
) {
}
