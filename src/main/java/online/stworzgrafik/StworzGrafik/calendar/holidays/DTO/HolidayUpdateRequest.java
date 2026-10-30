package online.stworzgrafik.StworzGrafik.calendar.holidays.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HolidayUpdateRequest(
        @NotBlank
        @Size(max = 100)
        String name
) {
}