package online.stworzgrafik.StworzGrafik.employee.delegation;


import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeDelegationEntityService {
    List<EmployeeDelegation> getEmployeeMonthlyDelegation(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);
}
