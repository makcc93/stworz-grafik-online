package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeVacationService {
    ResponseEmployeeVacationDTO createEmployeeProposalVacation(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull @Valid CreateEmployeeVacationDTO dto
    );

    ResponseEmployeeVacationDTO updateEmployeeVacation(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeVacationId,
            @NotNull @Valid UpdateEmployeeVacationDTO dto
    );

    ResponseEmployeeVacationDTO save(@NotNull EmployeeVacation employeeVacation);

    void delete(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeVacationId
    );

    ResponseEmployeeVacationDTO getById(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeVacationId
    );

    List<ResponseEmployeeVacationDTO> getByCriteria(@Nullable Long storeId,
                                                   @Nullable Long employeeId,
                                                   @Nullable Integer year,
                                                   @Nullable Integer month);

    boolean exists(@NotNull Long employeeVacationId);
}
