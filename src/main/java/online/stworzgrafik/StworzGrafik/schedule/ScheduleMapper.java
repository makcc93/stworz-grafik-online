package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface ScheduleMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "scheduleStatusName", source = "scheduleStatus.name")
    ResponseScheduleDTO toDTO(Schedule schedule);

    Schedule toEntity();

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSchedule(UpdateScheduleDTO dto, @MappingTarget Schedule schedule);
}
