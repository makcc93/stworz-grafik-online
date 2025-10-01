package online.stworzgrafik.StworzGrafik.store.DTO;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ResponseStoreDTO(
    Long id,
    String name,
    String storeCode,
    String location,
    Long branchId,
    String branchName,
    LocalDateTime createdAt,
    boolean enable,
    Long storeManagerId,
    LocalTime openForClientsHour,
    LocalTime closeForClientsHour
) {
}
