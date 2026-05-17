package online.stworzgrafik.StworzGrafik.schedule.hours.DTO;

import java.math.BigDecimal;

public record PeriodHoursCorrectionDTO(
        Long employeeId,
        String employeeFullName,
        BigDecimal calculatedHours,  // wyliczone z ScheduleDetails
        BigDecimal correctedHours    // ręczna korekta, null jeśli brak
) {}
