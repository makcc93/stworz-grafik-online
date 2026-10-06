package online.stworzgrafik.StworzGrafik.demo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoDeletedEvent;
import online.stworzgrafik.StworzGrafik.demo.lifecycle.DemoSessionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDeletedConsumer {
    private final DemoSessionService demoSessionService;

    @KafkaListener(
            topics = "DEMO_DELETED",
            groupId = "demo-deleted-handler"
    )
    public void consume(DemoDeletedEvent event){
        demoSessionService.markDeleted(event.userId());

        log.info(
                "KAFKA EVENT DELETED, lifecycle finished userId={}, storeId={}",
                event.userId(),
                event.storeId()
        );
    }
}
