package online.stworzgrafik.StworzGrafik.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoExpiredEvent;
import online.stworzgrafik.StworzGrafik.demo.lifecycle.DemoSession;
import online.stworzgrafik.StworzGrafik.demo.lifecycle.DemoSessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DemoExpirationScheduler {
    private final DemoEventProducer demoEventProducer;
    private final DemoSessionService demoSessionService;

    @Scheduled(fixedDelayString = "${app.demo.expiration-check-ms}")
    public void checkExpiredDemoAccounts(){
        Instant now = Instant.now();
        List<DemoSession> expired = demoSessionService.findExpired(now);

        for (DemoSession demoSession : expired){
            DemoExpiredEvent demoExpiredEvent = new DemoExpiredEvent(demoSession.getUserId(), demoSession.getStoreId());
            demoEventProducer.sendDemoExpiredEvent(demoExpiredEvent);
        }
    }
}
