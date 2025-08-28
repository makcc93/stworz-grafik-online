package online.stworzgrafik.StworzGrafik.shift.DTO;

import java.time.LocalTime;

public record ResponseShiftDTO(
    Integer id,
    LocalTime startHour,
    LocalTime endHour,
    Integer length
){}
