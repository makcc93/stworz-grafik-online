package online.stworzgrafik.StworzGrafik.store.DTO;

import online.stworzgrafik.StworzGrafik.store.RegionType;
import online.stworzgrafik.StworzGrafik.store.Store;

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
    boolean isEnable,
    Long storeManagerId,
    LocalTime openForClientsHour,
    LocalTime closeForClientsHour
) {
}
