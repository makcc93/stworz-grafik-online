package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
class ScheduleBuilder {
    public Schedule createSchedule(
            Store store,
            Integer year,
            Integer month,
            String name,
            ScheduleStatus scheduleStatus,
            Long createdByUserId
    ){
        return Schedule.builder()
                .store(store)
                .year(year)
                .month(month)
                .name(name)
                .scheduleStatus(scheduleStatus)
                .createdByUserId(createdByUserId)
                .build();
    }
}
