package online.stworzgrafik.StworzGrafik.shift.DTO;

import java.time.LocalTime;

public record ResponseShiftDTO(
    Long id,
    LocalTime startHour,
    LocalTime endHour,
    Integer length
){}
