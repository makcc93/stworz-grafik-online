package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaveEmployeeHoursConfirmationRequest(
        @NotEmpty @Valid List<UpdateEmployeeHoursConfirmationDTO> confirmations
) {
}