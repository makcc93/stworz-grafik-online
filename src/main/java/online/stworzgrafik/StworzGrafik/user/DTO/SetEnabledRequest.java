package online.stworzgrafik.StworzGrafik.user.DTO;

import jakarta.validation.constraints.NotNull;

public record SetEnabledRequest(
        @NotNull Boolean enabled
) {
}
