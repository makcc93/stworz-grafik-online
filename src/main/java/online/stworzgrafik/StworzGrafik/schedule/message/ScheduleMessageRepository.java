package online.stworzgrafik.StworzGrafik.schedule.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ScheduleMessageRepository extends JpaRepository<ScheduleMessage,Long>, JpaSpecificationExecutor<ScheduleMessage> {
}
