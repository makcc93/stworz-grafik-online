package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.EmployeeVacationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<ResponseEmployeeVacationDTO> getByCriteria(@Nullable Long storeId,
                                                    EmployeeVacationSpecificationDTO dto,
                                                    Pageable pageable);

    boolean exists(@NotNull Long employeeVacationId);
}
