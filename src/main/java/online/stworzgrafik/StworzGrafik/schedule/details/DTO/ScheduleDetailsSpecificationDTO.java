package online.stworzgrafik.StworzGrafik.schedule.details.DTO;

import java.time.LocalDate;

public record ScheduleDetailsSpecificationDTO(
    Long scheduleDetailsId,
    Long employeeId,
    LocalDate date,
    Long shiftId,
    Long shiftTypeConfigId)
    {}