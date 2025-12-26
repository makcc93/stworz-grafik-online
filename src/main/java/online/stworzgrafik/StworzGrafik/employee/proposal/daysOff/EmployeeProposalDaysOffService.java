package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmployeeProposalDaysOffService {
    ResponseEmployeeProposalDaysOffDTO createEmployeeProposalDaysOff(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull @Valid CreateEmployeeProposalDaysOffDTO dto);

    ResponseEmployeeProposalDaysOffDTO updateEmployeeProposalDaysOff(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull @Valid UpdateEmployeeProposalDaysOffDTO dto);

    ResponseEmployeeProposalDaysOffDTO save(
            @NotNull EmployeeProposalDaysOff employeeProposalDaysOff);

    void delete(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeProposalDaysOffId
    );

    ResponseEmployeeProposalDaysOffDTO findById(
            @NotNull Long storeId,
            @NotNull Long employeeId,
            @NotNull Long employeeProposalDaysOffId
    );

    boolean exists(@NotNull Long employeeProposalDaysOffId);
}
