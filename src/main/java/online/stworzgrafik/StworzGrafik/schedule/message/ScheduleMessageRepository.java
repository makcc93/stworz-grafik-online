package online.stworzgrafik.StworzGrafik.schedule.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

interface ScheduleMessageRepository extends JpaRepository<ScheduleMessage,Long>, JpaSpecificationExecutor<ScheduleMessage> {
    List<ScheduleMessage> findAllByScheduleIdAndYearAndMonth(Long scheduleId, Integer year, Integer month);
}
