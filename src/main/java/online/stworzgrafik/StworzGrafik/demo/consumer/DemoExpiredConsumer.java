package online.stworzgrafik.StworzGrafik.demo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoDeletedEvent;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoExpiredEvent;
import online.stworzgrafik.StworzGrafik.demo.DemoEventProducer;
import online.stworzgrafik.StworzGrafik.demo.DemoService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DemoExpiredConsumer {
    private final DemoEventProducer demoEventProducer;
    private final DemoService demoService;

    @KafkaListener(topics = "DEMO_EXPIRED", groupId = "demo-expired-handler")
    public void consume(DemoExpiredEvent demoExpiredEvent){
        Long userId = demoExpiredEvent.userId();
        Long storeId = demoExpiredEvent.storeId();

        log.info(
                "KAFKA DEMO_EXPIRED RECEIVED userId={}, storeId={}",
                userId,
                storeId
        );

        demoService.deleteDemo(userId, storeId);

        demoEventProducer.sendDemoDeletedEvent(
                new DemoDeletedEvent(
                        userId,
                        storeId
                )
        );
    }
}
