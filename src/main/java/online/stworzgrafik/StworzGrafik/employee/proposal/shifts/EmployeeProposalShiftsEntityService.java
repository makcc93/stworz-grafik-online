package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
public interface EmployeeProposalShiftsEntityService {
    List<EmployeeProposalShifts> findMonthlyEmployeeProposalShifts(@NotNull Long storeId,
                                                                   @NotNull Long employeeId,
                                                                   @NotNull LocalDate startDate,
                                                                   @NotNull LocalDate endDate);

    List<EmployeeProposalShifts> findMonthlyStoreProposalShifts(@NotNull Long storeId,
                                                                @NotNull LocalDate startDate,
                                                                @NotNull LocalDate endDate);
}
