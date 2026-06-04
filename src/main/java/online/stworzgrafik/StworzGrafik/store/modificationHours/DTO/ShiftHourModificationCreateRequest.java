package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Połączone DTO do tworzenia konfiguracji godzin zmian (POST).
 * Zastępuje dwa osobne @RequestBody (niedozwolone w Spring MVC).
 */
public record ShiftHourModificationCreateRequest(
        @NotNull List<@Valid ShiftHourModificationDTO> hours,
        @NotNull List<Long> excludedEmployeeIds
) {}

