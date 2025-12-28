package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record UpdateEmployeeProposalDaysOffDTO(
        @NotNull Integer year,
        @NotNull Integer month,

        @NotNull
        @Size(min=31, max = 31, message = "Employee monthly proposal days off array must have exactly 31 elements")
        int[] monthlyDaysOff,

        @Nullable LocalDateTime updatedAt
) {}
