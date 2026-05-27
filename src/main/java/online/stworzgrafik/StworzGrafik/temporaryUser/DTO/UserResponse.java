package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import online.stworzgrafik.StworzGrafik.temporaryUser.DirectorScope;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;

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
