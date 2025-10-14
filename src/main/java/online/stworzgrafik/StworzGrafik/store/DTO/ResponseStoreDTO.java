package online.stworzgrafik.StworzGrafik.store.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ResponseStoreDTO(
    @NotNull Long id,
    @NotNull String name,
    @NotNull String storeCode,
    @NotNull String location,
    @NotNull Long branchId,
    @NotNull String branchName,
    @NotNull LocalDateTime createdAt,
    @NotNull boolean enable,
    Long storeManagerId
) {
}
