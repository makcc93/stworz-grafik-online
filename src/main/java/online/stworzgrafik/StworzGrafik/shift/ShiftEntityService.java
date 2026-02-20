package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;

@Validated
public interface ShiftEntityService {
    Shift saveEntity(@NotNull Shift shift);
    Shift getEntityById(@NotNull Long id);
    Shift getArrayAsShift(@NotNull int[] array);
}
