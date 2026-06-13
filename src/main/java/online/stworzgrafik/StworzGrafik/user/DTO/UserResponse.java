package online.stworzgrafik.StworzGrafik.user.DTO;

import online.stworzgrafik.StworzGrafik.user.DirectorScope;
import online.stworzgrafik.StworzGrafik.user.UserRole;

public record UserResponse(
        Long id,
        String login,
        UserRole role,
        DirectorScope directorScope,
        Long storeId,
        Long branchId,
        Long regionId,
        Boolean enabled
) {
}
