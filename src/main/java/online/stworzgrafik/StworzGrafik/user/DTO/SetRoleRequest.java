package online.stworzgrafik.StworzGrafik.user.DTO;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.user.UserRole;

public record SetRoleRequest(
        @NotNull UserRole userRole
        ) {
}
