package online.stworzgrafik.StworzGrafik.employee.vacation;

import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface EmployeeVacationMapper {
    @Mapping(source = "store.id" ,target = "storeId")
    @Mapping(source = "employee.id" ,target = "employeeId")
    ResponseEmployeeVacationDTO toResponseEmployeeVacationDTO(EmployeeVacation employeeVacation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeVacation(UpdateEmployeeVacationDTO updateEmployeeVacationDTO, @MappingTarget EmployeeVacation employeeVacation);
}
