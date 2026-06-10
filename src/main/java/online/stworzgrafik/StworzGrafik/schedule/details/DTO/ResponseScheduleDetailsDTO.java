package online.stworzgrafik.StworzGrafik.schedule.details.DTO;

import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ResponseScheduleDetailsDTO(
        Long id,
        Long scheduleId,
        Long employeeId,
        LocalDate date,
        Long shiftId,
        Long shiftTypeConfigId,
        LocalTime startHour,
        LocalTime endHour,
        ShiftCode shiftCode,
        BigDecimal defaultHours,   // godziny z ShiftTypeConfig — używane dla urlopu/L4 (shift ma 00:00→00:00)
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
