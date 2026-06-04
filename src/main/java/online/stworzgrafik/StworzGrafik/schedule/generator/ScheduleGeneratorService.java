package online.stworzgrafik.StworzGrafik.schedule.generator;

public interface ScheduleGeneratorService {
    byte[] generateSchedule(Long storeId, Long scheduleId);
}
