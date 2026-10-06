package online.stworzgrafik.StworzGrafik.demo.lifecycle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface DemoSessionRepository extends JpaRepository<DemoSession,Long> {
    List<DemoSession> findAllByStatusAndExpiresAtLessThanEqual(DemoSessionStatus status, Instant expiresAt);
    Optional<DemoSession> findByUserId(Long userId);
}
