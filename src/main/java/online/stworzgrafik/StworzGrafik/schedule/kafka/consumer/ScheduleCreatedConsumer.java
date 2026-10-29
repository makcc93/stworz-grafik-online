package online.stworzgrafik.StworzGrafik.schedule.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.email.DTO.EmailSendingDTO;
import online.stworzgrafik.StworzGrafik.email.EmailService;
import online.stworzgrafik.StworzGrafik.schedule.kafka.DTO.ScheduleCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleCreatedConsumer {

    private final EmailService emailService;

    @Value("${app.mail.admin}")
    private String adminEmail;
    
    @KafkaListener(
            topics = "SCHEDULE_CREATED",
            groupId = "schedule-service"
    )
    public void consumeScheduleCreatedEvent(ConsumerRecord<String, ScheduleCreatedEvent> record){
        ScheduleCreatedEvent event = record.value();

        EmailSendingDTO emailSendingDTO = new EmailSendingDTO(
                adminEmail,
                "Utworzony został nowy grafik " + event.storeName() + " (" + event.storeCode() + ") na miesiąc " + event.month() + "/" + event.year(),
                event.createdAt().toString()
        );
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
                event,
                emailSendingDTO.title()
        );
    }
}
