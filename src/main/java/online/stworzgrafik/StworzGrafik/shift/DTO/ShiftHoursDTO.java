package online.stworzgrafik.StworzGrafik.shift.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ShiftHoursDTO (
        @NotNull
        LocalTime startHour,

        @NotNull
        LocalTime endHour
){}
