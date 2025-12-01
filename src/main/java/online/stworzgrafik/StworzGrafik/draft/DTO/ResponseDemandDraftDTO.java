package online.stworzgrafik.StworzGrafik.draft.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record ResponseDemandDraftDTO(
        Long id,
        Store store,
        Employee employee,
        Integer year,
        Integer month,
        Integer day,
        int[] hourlyDemand,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
