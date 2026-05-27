package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String login,
        @NotBlank String password
) {
}
