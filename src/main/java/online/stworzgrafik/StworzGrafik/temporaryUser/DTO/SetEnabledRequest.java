package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import jakarta.validation.constraints.NotNull;

public record SetEnabledRequest(
        @NotNull Boolean enabled
) {
}
