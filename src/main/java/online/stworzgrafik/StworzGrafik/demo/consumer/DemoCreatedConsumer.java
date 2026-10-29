package online.stworzgrafik.StworzGrafik.demo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.demo.DTO.DemoCreatedEvent;
import online.stworzgrafik.StworzGrafik.email.DTO.EmailSendingDTO;
import online.stworzgrafik.StworzGrafik.email.EmailService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoCreatedConsumer {

    private final EmailService emailService;

    @Value("${app.mail.admin}")
    private String adminEmail;

    @KafkaListener(
            topics = "DEMO_CREATED",
            groupId = "demo-service"
    )
    public void consumeDemoCreatedEvent(ConsumerRecord<String, DemoCreatedEvent> record) {
        DemoCreatedEvent createdEvent = record.value();

        EmailSendingDTO emailSendingDTO = new EmailSendingDTO(adminEmail, "Utworzono nowe konto demonstracyjne", LocalDateTime.now().toString());
        emailService.sendEmail(emailSendingDTO);

        log.info(
                """
                
                <<< KAFKA CONSUMER | RECEIVED
                topic={}
                key={}
                partition={}
                offset={}
                event={}
                mailSendTitle={}
                """,
                record.topic(),
                record.key(),
                record.partition(),
                record.offset(),
                createdEvent,
                emailSendingDTO.title()
        );
    }
}
