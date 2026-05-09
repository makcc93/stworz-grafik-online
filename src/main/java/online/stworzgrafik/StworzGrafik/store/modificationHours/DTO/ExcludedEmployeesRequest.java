package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExcludedEmployeesRequest(
        @NotNull List<Long> excludedEmployeeIds
) {}
