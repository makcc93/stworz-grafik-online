package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.temporaryUser.DirectorScope;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;

public record CreateUserRequest(
        @NotBlank String login,
        @NotBlank @Size(min = 6) String password,
        @NotNull UserRole role,
        Long storeId,
        Long branchId,
        Long regionId,
        DirectorScope directorScope
) {
}
