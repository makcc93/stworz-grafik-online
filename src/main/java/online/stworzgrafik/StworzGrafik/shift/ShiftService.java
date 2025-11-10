package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.List;

@Validated
public interface ShiftService {
    public ResponseShiftDTO save(@NotNull Shift shift);
    public ResponseShiftDTO create(@NotNull ShiftHoursDTO shiftHoursDTO);
    public ResponseShiftDTO updateShift(@NotNull Long shiftId, @NotNull ShiftHoursDTO shiftHoursDTO);

    public void delete(@NotNull Long id);
    public List<ResponseShiftDTO> findAll();
    public ResponseShiftDTO findById(@NotNull Long id);
    public boolean exists(@NotNull LocalTime startHour, @NotNull LocalTime endHour);
    public boolean exists(@NotNull Long id);
}
