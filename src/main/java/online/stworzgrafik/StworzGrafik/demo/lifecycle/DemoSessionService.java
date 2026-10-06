package online.stworzgrafik.StworzGrafik.demo.lifecycle;

import online.stworzgrafik.StworzGrafik.demo.DTO.DemoCreatedEvent;

import java.time.Instant;
import java.util.List;

public interface DemoSessionService {
    void register(DemoCreatedEvent demoCreatedEvent);
    List<DemoSession> findExpired(Instant now);
    void markDeleted(Long userId);
}
