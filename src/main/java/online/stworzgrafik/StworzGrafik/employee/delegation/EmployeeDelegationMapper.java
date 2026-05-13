package online.stworzgrafik.StworzGrafik.employee.delegation;

import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.ResponseEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.UpdateEmployeeDelegationDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy =  ReportingPolicy.IGNORE)
interface EmployeeDelegationMapper {
    @Mapping(source = "store.id" ,target = "storeId")
    @Mapping(source = "employee.id" ,target = "employeeId")
    ResponseEmployeeDelegationDTO toResponseEmployeeDelegationDTO(EmployeeDelegation employeeDelegation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeDelegation(UpdateEmployeeDelegationDTO dto, @MappingTarget EmployeeDelegation employeeDelegation);
}
