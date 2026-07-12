package online.stworzgrafik.StworzGrafik.draft.DTO;

import java.math.BigDecimal;

public record MonthlyNormResponseDTO(
        int standardWorkingHours,
        BigDecimal totalEmployeeNorm,
        int activeNonWarehouseCount,
        boolean usingConfirmedHours
) {}
