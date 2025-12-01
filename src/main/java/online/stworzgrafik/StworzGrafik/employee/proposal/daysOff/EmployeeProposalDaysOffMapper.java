package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
interface EmployeeProposalDaysOffMapper {
    ResponseEmployeeProposalDaysOffDTO toResponseEmployeeProposalDaysOffDTO(EmployeeProposalDaysOff employeeProposalDaysOff);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeProposalDaysOff(UpdateEmployeeProposalDaysOffDTO updateEmployeeProposalDaysOffDTO, @MappingTarget EmployeeProposalDaysOff employeeProposalDaysOff);
}

