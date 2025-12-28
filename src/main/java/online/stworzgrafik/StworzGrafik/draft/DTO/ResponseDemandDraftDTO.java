package online.stworzgrafik.StworzGrafik.draft.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResponseDemandDraftDTO(
        Long id,
        Long storeId,
        LocalDate draftDate,
        int[] hourlyDemand,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
