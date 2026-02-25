package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeVacationEntityService {
    List<EmployeeVacation> getEmployeeMonthlyVacation(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);
}
