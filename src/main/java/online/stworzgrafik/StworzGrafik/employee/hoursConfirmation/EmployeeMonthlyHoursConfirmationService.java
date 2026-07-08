package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.EmployeeHoursConfirmationDTO;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.SaveEmployeeHoursConfirmationRequest;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeMonthlyHoursConfirmationService {
    List<EmployeeHoursConfirmationDTO> getHoursConfirmationForMonth(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);

    List<EmployeeHoursConfirmationDTO> saveHoursConfirmation(@NotNull Long storeId,
                                                             @NotNull Integer year,
                                                             @NotNull Integer month,
                                                             @NotNull @Valid SaveEmployeeHoursConfirmationRequest request);
}