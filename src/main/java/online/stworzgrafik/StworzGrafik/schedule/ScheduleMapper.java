package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface ScheduleMapper {

    default ScheduleStatus stringToEnum(String scheduleStatusName){
        if (scheduleStatusName == null){
            return null;
        }

        try {
            return ScheduleStatus.valueOf(scheduleStatusName.toUpperCase());
        }
        catch (IllegalArgumentException exception){
            throw new IllegalArgumentException("Unknown Schedule Status, " + exception);
        }
    }

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "scheduleStatusName", source = "scheduleStatus")
    ResponseScheduleDTO toDTO(Schedule schedule);


    @Mapping(target = "store.id", source = "storeId")
    @Mapping(target = "scheduleStatus", source = "scheduleStatusName")
    Schedule toEntity(ResponseScheduleDTO responseScheduleDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSchedule(UpdateScheduleDTO dto, @MappingTarget Schedule schedule);
}
