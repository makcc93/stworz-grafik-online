package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
interface EmployeeProposalShiftsMapper {
    ResponseEmployeeProposalShiftsDTO toResponseEmployeeProposalShiftsDTO(EmployeeProposalShifts employeeProposalShifts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeProposalShifts(UpdateEmployeeProposalShiftsDTO updateEmployeeProposalShiftsDTO, @MappingTarget EmployeeProposalShifts employeeProposalShifts);
}
