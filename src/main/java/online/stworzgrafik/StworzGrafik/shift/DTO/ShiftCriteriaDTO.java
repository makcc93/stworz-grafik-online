package online.stworzgrafik.StworzGrafik.shift.DTO;

import java.time.LocalTime;

public record ShiftCriteriaDTO(
        LocalTime startHour,
        LocalTime endHour
) {}
