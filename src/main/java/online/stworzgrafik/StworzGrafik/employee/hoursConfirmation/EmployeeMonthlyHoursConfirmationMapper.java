package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.EmployeeHoursConfirmationDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
class EmployeeMonthlyHoursConfirmationMapper {

    EmployeeHoursConfirmationDTO toResponseDTO(Employee employee,
                                               BigDecimal defaultNormHours,
                                               Optional<EmployeeMonthlyHoursConfirmation> existing) {
        BigDecimal confirmedHours = existing
                .map(EmployeeMonthlyHoursConfirmation::getConfirmedHours)
                .orElse(defaultNormHours);

        return new EmployeeHoursConfirmationDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                defaultNormHours,
                confirmedHours,
                existing.isPresent()
        );
    }
}