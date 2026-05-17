package online.stworzgrafik.StworzGrafik.schedule.generator;

import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;

public interface ScheduleGeneratorService {
    ResponseScheduleDTO generateSchedule(Long storeId, Long scheduleId);
}
