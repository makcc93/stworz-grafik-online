package online.stworzgrafik.StworzGrafik.email.DTO;

public record EmailSendingDTO(
        String to,
        String title,
        String message
) {
}
