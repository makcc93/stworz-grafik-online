package online.stworzgrafik.StworzGrafik.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoCreatedEvent;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoDeletedEvent;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoExpiredEvent;
import online.stworzgrafik.StworzGrafik.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DemoEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDemoCreatedEvent(DemoCreatedEvent event) {
        send(
                KafkaTopics.DEMO_CREATED,
                event.userId(),
                event
        );
    }

    public void sendDemoExpiredEvent(DemoExpiredEvent event){
        send(
                KafkaTopics.DEMO_EXPIRED,
                event.userId(),
                event
        );
    }

    public void sendDemoDeletedEvent(DemoDeletedEvent event){
        send(
                KafkaTopics.DEMO_DELETED,
                event.userId(),
                event
        );
    }

    private void send(KafkaTopics topics, Long key, Object event){
        kafkaTemplate.send(topics.name(),key.toString(),event)
                .whenComplete((result, exeption) ->
                {
                 if (exeption != null){
                     log.error(
                             "Kafka send failed topic={}, event={}",
                             topics,
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
