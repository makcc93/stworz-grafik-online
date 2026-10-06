package online.stworzgrafik.StworzGrafik.demo.DTO;

import java.time.Instant;

public record DemoResponse(
        String login,
        String role,
        Long storeId,
        Instant expiresAt
) {
}
