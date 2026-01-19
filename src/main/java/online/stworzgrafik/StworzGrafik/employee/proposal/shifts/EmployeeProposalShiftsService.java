package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.EmployeeProposalShiftsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
public interface EmployeeProposalShiftsService {
    ResponseEmployeeProposalShiftsDTO createEmployeeProposalShift(@NotNull Long storeId,
                                                                  @NotNull Long employeeId,
                                                                  @NotNull @Valid CreateEmployeeProposalShiftsDTO dto);

    ResponseEmployeeProposalShiftsDTO updateEmployeeProposalShift(@NotNull Long storeId,
                                                                  @NotNull Long employeeId,
                                                                  @NotNull Long employeeProposalShiftId,
                                                                  @NotNull @Valid UpdateEmployeeProposalShiftsDTO dto);

    ResponseEmployeeProposalShiftsDTO save(@NotNull EmployeeProposalShifts employeeProposalShifts);

    void delete(@NotNull Long storeId,
                @NotNull Long employeeId,
                @NotNull Long employeeProposalShiftId
    );

    ResponseEmployeeProposalShiftsDTO getById(@NotNull Long storeId,
                                              @NotNull Long employeeId,
                                              @NotNull Long employeeProposalShiftId);

    List<ResponseEmployeeProposalShiftsDTO> getAll();

    boolean exists(@NotNull Long employeeProposalShiftId);

    List<ResponseEmployeeProposalShiftsDTO> getByCriteria(@Nullable Long storeId,
                                                          EmployeeProposalShiftsSpecificationDTO dto);
}
