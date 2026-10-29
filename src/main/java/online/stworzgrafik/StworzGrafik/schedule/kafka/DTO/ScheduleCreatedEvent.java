package online.stworzgrafik.StworzGrafik.schedule.kafka.DTO;

import jakarta.persistence.criteria.CriteriaBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduleCreatedEvent(
        Long userId,
        Long storeId,
        String storeCode,
        String storeName,
        Integer year,
        Integer month,
        LocalDateTime createdAt
) {
}
