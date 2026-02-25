package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeProposalDaysOffEntityService {
    List<EmployeeProposalDaysOff> getEmployeeMonthlyProposalDaysOff(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);
}
