package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO;

import jakarta.persistence.*;
import online.stworzgrafik.StworzGrafik.converter.IntArrayJsonConverter;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

public record ResponseEmployeeProposalDaysOffDTO(
        Long id,
        Store store,
        Employee employee,
        Integer year,
        Integer month,
        int[] monthlyDaysOff,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
)
{}
