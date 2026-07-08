package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO;

import java.math.BigDecimal;

public record EmployeeHoursConfirmationDTO(
        Long employeeId,
        String employeeFirstName,
        String employeeLastName,
        BigDecimal defaultNormHours,
        BigDecimal confirmedHours,
        boolean confirmed
) {
}