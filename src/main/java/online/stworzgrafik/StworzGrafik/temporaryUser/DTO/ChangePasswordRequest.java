package online.stworzgrafik.StworzGrafik.temporaryUser.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(min = 6) String newPassword
) {
}
