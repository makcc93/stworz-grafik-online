package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ShiftHourMappingRequest(
        @NotNull List<@Valid ShiftHourModificationDTO> hours
) {}
