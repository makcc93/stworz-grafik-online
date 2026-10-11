package online.stworzgrafik.StworzGrafik.email;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.email.DTO.EmailSendingDTO;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailSendingDTO dto) {

    }
}
