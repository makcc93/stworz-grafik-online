package online.stworzgrafik.StworzGrafik.schedule.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.kafka.KafkaTopics;
import online.stworzgrafik.StworzGrafik.schedule.kafka.DTO.ScheduleCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendScheduleCreatedEvent(ScheduleCreatedEvent event){
        kafkaTemplate.send(KafkaTopics.SCHEDULE_CREATED.name(),event.userId().toString(),event)
                .whenComplete((result, exeption) -> {
                    if (exeption != null){
                        log.error(
                                "Kafka send failed topic={}, event={}",
                                KafkaTopics.SCHEDULE_CREATED.name(),
                                event,
                                exeption
                        );
                        return;
                    }

                    log.info(
                            "KAFKA SUCCESS topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}
