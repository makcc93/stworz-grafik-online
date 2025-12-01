package online.stworzgrafik.StworzGrafik.employee.vacation;

import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
interface EmployeeVacationMapper {
    ResponseEmployeeVacationDTO toResponseEmployeeVacationDTO(EmployeeVacation employeeVacation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeVacation(UpdateEmployeeVacationDTO updateEmployeeVacationDTO, @MappingTarget EmployeeVacation employeeVacation);
}
