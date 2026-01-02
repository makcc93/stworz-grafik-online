package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

public record CreateEmployeeProposalShiftsDTO(
        @NotNull
        @Min(2000)
        @Max(2099)
        LocalDate date,

        @NotNull
        @Size(min = 24, max = 24)
        int[] dailyProposalShift
        ) {
}
