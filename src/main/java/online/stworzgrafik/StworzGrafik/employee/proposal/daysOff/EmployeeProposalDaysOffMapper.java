package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface EmployeeProposalDaysOffMapper {
    @Mapping(source = "store.id" ,target = "storeId")
    @Mapping(source = "employee.id" ,target = "employeeId")
    ResponseEmployeeProposalDaysOffDTO toResponseEmployeeProposalDaysOffDTO(EmployeeProposalDaysOff employeeProposalDaysOff);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeProposalDaysOff(UpdateEmployeeProposalDaysOffDTO updateEmployeeProposalDaysOffDTO, @MappingTarget EmployeeProposalDaysOff employeeProposalDaysOff);
}

