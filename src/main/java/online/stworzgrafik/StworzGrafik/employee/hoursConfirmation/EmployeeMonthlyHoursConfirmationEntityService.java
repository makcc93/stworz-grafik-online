package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeMonthlyHoursConfirmationEntityService {
    List<EmployeeMonthlyHoursConfirmation> getStoreMonthlyHoursConfirmations(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);
}