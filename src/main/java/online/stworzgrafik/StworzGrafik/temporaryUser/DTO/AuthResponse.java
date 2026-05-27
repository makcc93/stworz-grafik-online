package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import jakarta.annotation.Nullable;

public record AuthResponse(
        String token,
        String login,
        String role,
        @Nullable Long storeId
) {
}
