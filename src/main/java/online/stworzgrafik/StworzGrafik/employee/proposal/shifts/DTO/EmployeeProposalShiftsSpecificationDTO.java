package online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO;

import java.time.LocalDate;

public record EmployeeProposalShiftsSpecificationDTO(
        Long employeeId,
        LocalDate startDate,
        LocalDate endDate
) {
}
