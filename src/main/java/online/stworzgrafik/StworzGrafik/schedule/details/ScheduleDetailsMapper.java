package online.stworzgrafik.StworzGrafik.schedule.details;

import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface ScheduleDetailsMapper {
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "shift.id", target = "shiftId")
    @Mapping(source = "shiftTypeConfig.id", target = "shiftTypeConfigId")
    ResponseScheduleDetailsDTO toDTO(ScheduleDetails scheduleDetails);

//    ScheduleDetails toEntity();

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateScheduleDetails(UpdateScheduleDetailsDTO dto, @MappingTarget ScheduleDetails scheduleDetails);
}