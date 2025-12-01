package online.stworzgrafik.StworzGrafik.employee.vacation.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record ResponseEmployeeVacationDTO(
        Long id,
        Store store,
        Employee employee,
        Integer year,
        Integer month,
        int[] monthlyVacation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
