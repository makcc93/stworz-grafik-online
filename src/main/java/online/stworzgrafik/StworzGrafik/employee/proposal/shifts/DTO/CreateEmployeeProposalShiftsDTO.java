package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEmployeeProposalShiftsDTO(
        @NotNull
        LocalDate date,

        @NotNull
        @Size(min = 24, max = 24)
        int[] dailyProposalShift
        ) {
}
