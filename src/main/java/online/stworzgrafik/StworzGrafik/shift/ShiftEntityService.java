package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.List;

@Validated
public interface ShiftEntityService {
    Shift saveEntity(@NotNull Shift shift);
    List<Shift> saveAll(List<Shift> shifts);
    Shift getEntityById(@NotNull Long id);
    Shift getEntityByHours(@NotNull LocalTime startHour, @NotNull LocalTime endHour);
    Shift getArrayAsShift(@NotNull int[] array);
    Shift updateShift(ShiftHoursDTO dto, Shift shift);
    List<Shift> getAll();
}
