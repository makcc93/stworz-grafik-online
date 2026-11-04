package online.stworzgrafik.StworzGrafik.employee;

import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface EmployeeMapper {

    @Mapping(source = "store.id" ,target = "storeId")
    @Mapping(source = "position.id", target = "positionId")
    @Mapping(source = "specialWorkNorm.id", target = "specialWorkNormId")
    @Mapping(source = "specialWorkNorm.name", target = "specialWorkNormName")
    ResponseEmployeeDTO toResponseEmployeeDTO(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isSpecial", ignore = true)
    @Mapping(target = "specialWorkNorm", ignore = true)
    void updateEmployee(UpdateEmployeeDTO updateEmployeeDTO, @MappingTarget Employee employee);

    default ResponseEmployeeDTO toDto(Employee employee) {
        return toResponseEmployeeDTO(employee);
    }
}