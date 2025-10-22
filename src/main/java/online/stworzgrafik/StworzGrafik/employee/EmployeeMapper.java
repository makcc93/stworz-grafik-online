package online.stworzgrafik.StworzGrafik.employee;

import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {


    @Mapping(source = "store.id" ,target = "storeId")
    @Mapping(source = "position.id", target = "positionId")
    ResponseEmployeeDTO toResponseEmployeeDTO(Employee employee);

    @Mapping(source = "storeId", target = "store.id")
    @Mapping(source = "positionId", target = "position.id")
    Employee toEmployee(ResponseEmployeeDTO responseEmployeeDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployee(UpdateEmployeeDTO updateEmployeeDTO, @MappingTarget Employee employee);
}
