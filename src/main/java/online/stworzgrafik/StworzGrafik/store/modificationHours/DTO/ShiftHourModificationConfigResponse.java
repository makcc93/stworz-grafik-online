package online.stworzgrafik.StworzGrafik.store.modificationHours.DTO;

import java.util.List;

public record ShiftHourModificationConfigResponse(
        List<ShiftHourModificationDTO> hours,
        List<Long> excludedEmployeeIds
) {}
