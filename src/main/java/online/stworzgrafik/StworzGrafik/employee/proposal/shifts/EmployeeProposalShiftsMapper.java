package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface EmployeeProposalShiftsMapper {
    @Mapping(source = "store.id" ,target = "storeId")
    @Mapping(source = "employee.id" ,target = "employeeId")
    ResponseEmployeeProposalShiftsDTO toResponseEmployeeProposalShiftsDTO(EmployeeProposalShifts employeeProposalShifts);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeProposalShifts(UpdateEmployeeProposalShiftsDTO updateEmployeeProposalShiftsDTO, @MappingTarget EmployeeProposalShifts employeeProposalShifts);
}
