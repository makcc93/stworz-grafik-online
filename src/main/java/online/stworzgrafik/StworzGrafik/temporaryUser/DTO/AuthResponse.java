package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.temporaryUser.DirectorScope;

public record AuthResponse(
        String token,
        String login,
        String role,
        @Nullable Long storeId,
        @Nullable DirectorScope directorScope,
        @Nullable String scopeName
) {
}
