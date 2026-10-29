package online.stworzgrafik.StworzGrafik.email;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.email.DTO.EmailSendingDTO;

public interface EmailService {
    void sendEmail(@NotNull EmailSendingDTO dto);
}
