package online.stworzgrafik.StworzGrafik.user.DTO;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.user.DirectorScope;

public record AuthResponse(
        String token,
        String login,
        String role,
        @Nullable Long storeId,
        @Nullable DirectorScope directorScope,
        @Nullable String scopeName
) {
}
