package online.stworzgrafik.StworzGrafik.employee.vacation.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record UpdateEmployeeVacationDTO(
        Integer year,
        Integer month,
        int[] monthlyVacation,
        LocalDateTime updatedAt
) {
}
