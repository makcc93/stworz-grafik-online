package online.stworzgrafik.StworzGrafik.demo.lifecycle;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoCreatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoSessionServiceImpl implements DemoSessionService{
    private final DemoSessionRepository demoSessionRepository;

    @Override
    @Transactional
    public void register(DemoCreatedEvent demoCreatedEvent) {
        DemoSession demoSession = DemoSession.builder()
                .userId(demoCreatedEvent.userId())
                .storeId(demoCreatedEvent.storeId())
                .expiresAt(demoCreatedEvent.expiresAt())
                .status(DemoSessionStatus.ACTIVE)
                .build();

        demoSessionRepository.save(demoSession);
    }

    @Override
    public List<DemoSession> findExpired(Instant now) {
        return demoSessionRepository.findAllByStatusAndExpiresAtLessThanEqual(DemoSessionStatus.ACTIVE,now);
    }

    @Override
    @Transactional
    public void markDeleted(Long userId) {
        demoSessionRepository.findByUserId(userId)
                .ifPresent(DemoSession::markDeleted);
    }
}
