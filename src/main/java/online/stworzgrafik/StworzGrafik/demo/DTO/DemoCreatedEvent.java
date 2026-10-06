package online.stworzgrafik.StworzGrafik.demo.DTO;

import java.time.Instant;

public record DemoCreatedEvent(
        Long userId,
        Long storeId,
        Instant expiresAt
) {
}
