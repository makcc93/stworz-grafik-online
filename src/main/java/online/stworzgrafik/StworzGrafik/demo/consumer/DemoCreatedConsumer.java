package online.stworzgrafik.StworzGrafik.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoCreatedConsumer {
    @KafkaListener(
            topics = "DEMO_CREATED",
            groupId = "demo-service"
    )
    public void consumeDemoCreatedEvent(ConsumerRecord<String, DemoCreatedEvent> record) {

        DemoCreatedEvent createdEvent = record.value();

        log.info(
                """
                
                <<< KAFKA CONSUMER | RECEIVED
                topic={}
                key={}
                partition={}
                offset={}
                event={}
                """,
                record.topic(),
                record.key(),
                record.partition(),
                record.offset(),
                createdEvent
        );
    }
}
