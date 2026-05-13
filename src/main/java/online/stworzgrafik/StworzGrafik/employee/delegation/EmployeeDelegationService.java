package online.stworzgrafik.StworzGrafik.employee.delegation;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.CreateEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.EmployeeDelegationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.ResponseEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.UpdateEmployeeDelegationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeDelegationService {
    ResponseEmployeeDelegationDTO createEmployeeProposalDelegation(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull @Valid CreateEmployeeDelegationDTO dto
    );

    ResponseEmployeeDelegationDTO updateEmployeeDelegation(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeDelegationId,
            @NotNull @Valid UpdateEmployeeDelegationDTO dto
    );

    ResponseEmployeeDelegationDTO save(@NotNull EmployeeDelegation employeeDelegation);

    void delete(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeDelegationId
    );

    ResponseEmployeeDelegationDTO getById(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeDelegationId
    );

    Page<ResponseEmployeeDelegationDTO> getByCriteria(@Nullable Long storeId,
                                                    EmployeeDelegationSpecificationDTO dto,
                                                    Pageable pageable);

    boolean exists(@NotNull Long employeeDelegationId);
}
