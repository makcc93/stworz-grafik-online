package online.stworzgrafik.StworzGrafik.schedule.details;

import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface ScheduleDetailsMapper {
    @Mapping(source = "schedule.id",                    target = "scheduleId")
    @Mapping(source = "employee.id",                    target = "employeeId")
    @Mapping(source = "shift.id",                       target = "shiftId")
    @Mapping(source = "shift.startHour",                target = "startHour")
    @Mapping(source = "shift.endHour",                  target = "endHour")
    @Mapping(source = "shiftTypeConfig.id",             target = "shiftTypeConfigId")
    @Mapping(source = "shiftTypeConfig.code",           target = "shiftCode")
    @Mapping(source = "shiftTypeConfig.defaultHours",   target = "defaultHours")
    ResponseScheduleDetailsDTO toDTO(ScheduleDetails scheduleDetails);
}
