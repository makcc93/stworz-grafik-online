package online.stworzgrafik.StworzGrafik.schedule.generator;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.MonthlyStoreScheduleGenerator;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleMapper;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleStatus;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ScheduleGeneratorServiceImpl implements ScheduleGeneratorService{
    private final ScheduleEntityService scheduleEntityService;
    private final MonthlyStoreScheduleGenerator monthlyStoreScheduleGenerator;
    private final UserAuthorizationService userAuthorizationService;
    private final ScheduleMapper mapper;

    @Override
    public byte[] generateSchedule(Long storeId, Long scheduleId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store: " + storeId);
        }

        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);

        try {
            byte[] result = monthlyStoreScheduleGenerator.generateMonthlySchedule(
                    storeId,
                    schedule.getYear(),
                    schedule.getMonth()
            );

            schedule.setScheduleStatus(ScheduleStatus.DONE);
            scheduleEntityService.saveEntity(schedule);

            return result;
        } catch (IOException e) {
            schedule.setScheduleStatus(ScheduleStatus.FAILED);
            scheduleEntityService.saveEntity(schedule);
            throw new RuntimeException("Error generating schedule: " + e.getMessage(), e);
        }
    }


}
