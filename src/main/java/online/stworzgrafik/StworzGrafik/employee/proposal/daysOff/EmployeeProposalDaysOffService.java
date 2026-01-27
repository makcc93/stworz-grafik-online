package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.EmployeeProposalDaysOffSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmployeeProposalDaysOffService {
    ResponseEmployeeProposalDaysOffDTO createEmployeeProposalDaysOff(@NotNull Long storeId,
                                                                     @NotNull Long employeeId,
                                                                     @NotNull @Valid CreateEmployeeProposalDaysOffDTO dto
    );

    ResponseEmployeeProposalDaysOffDTO updateEmployeeProposalDaysOff(@NotNull Long storeId,
                                                                     @NotNull Long employeeId,
                                                                     @NotNull Long employeeProposalDaysOffId,
                                                                     @NotNull @Valid UpdateEmployeeProposalDaysOffDTO dto
    );

    ResponseEmployeeProposalDaysOffDTO save(@NotNull EmployeeProposalDaysOff employeeProposalDaysOff);

    void delete(@NotNull Long storeId,
                @NotNull Long employeeId,
                @NotNull Long employeeProposalDaysOffId
    );

    ResponseEmployeeProposalDaysOffDTO getById(@NotNull Long storeId,
                                               @NotNull Long employeeId,
                                               @NotNull Long employeeProposalDaysOffId
    );

    Page<ResponseEmployeeProposalDaysOffDTO> getByCriteria(@NotNull Long storeId,
                                                           @Nullable EmployeeProposalDaysOffSpecificationDTO dto,
                                                           Pageable pageable);

    boolean exists(@NotNull Long employeeProposalDaysOffId);
}
