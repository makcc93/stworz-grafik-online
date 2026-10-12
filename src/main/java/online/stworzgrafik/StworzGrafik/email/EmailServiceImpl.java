package online.stworzgrafik.StworzGrafik.email;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.email.DTO.EmailSendingDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
class EmailServiceImpl implements EmailService{

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendEmail(EmailSendingDTO dto) {
        Objects.requireNonNull(dto, "Sending email data transfer object cannot be null");

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(dto.to());
        simpleMailMessage.setSubject(dto.title());
        simpleMailMessage.setText(dto.message());

        javaMailSender.send(simpleMailMessage);
    }
}
