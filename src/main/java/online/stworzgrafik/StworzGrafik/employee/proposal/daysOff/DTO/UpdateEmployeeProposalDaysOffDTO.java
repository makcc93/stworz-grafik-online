package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record UpdateEmployeeProposalDaysOffDTO(
        Store store,
        Employee employee,
        Integer year,
        Integer month,
        int[] monthlyDaysOff,
        @Nullable LocalDateTime updatedAt
) {}
