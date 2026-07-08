package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateEmployeeHoursConfirmationDTO(
        @NotNull Long employeeId,
        @NotNull BigDecimal confirmedHours
) {
}