package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;

import java.time.LocalTime;
import java.util.List;

public interface ShiftService {
    ResponseShiftDTO saveDto(@NotNull ShiftHoursDTO shiftHoursDTO);
    Shift saveEntity(@NotNull Shift shift);
    ResponseShiftDTO create(@NotNull ShiftHoursDTO shiftHoursDTO);
    Shift findEntityById(@NotNull Long id);
    void delete(@NotNull Long id);
    List<ResponseShiftDTO> findAll();
    ResponseShiftDTO findById(@NotNull Long id);
    boolean exists(@NotNull LocalTime startHour, @NotNull LocalTime endHour);
    boolean exists(@NotNull Long id);

}
