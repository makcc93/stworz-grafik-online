package online.stworzgrafik.StworzGrafik.calendar.holidays.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HolidayCreateRequest(
        @NotNull
        LocalDate date,

        @NotBlank
        @Size(max = 100)
        String name
) {
}