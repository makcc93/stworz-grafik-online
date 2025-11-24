package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Validated
public interface ShiftService {
    ResponseShiftDTO save(@NotNull Shift shift);
    ResponseShiftDTO create(@NotNull ShiftHoursDTO shiftHoursDTO);
    ResponseShiftDTO updateShift(@NotNull Long shiftId, @NotNull ShiftHoursDTO shiftHoursDTO);

    void delete(@NotNull Long id);
    List<ResponseShiftDTO> findAll();
    ResponseShiftDTO findById(@NotNull Long id);
    boolean exists(@NotNull LocalTime startHour, @NotNull LocalTime endHour);
    boolean exists(@NotNull Long id);

    //test methods below
    Integer getLength(@NotNull @Valid ShiftHoursDTO shiftHoursDTO);
    BigDecimal getDurationHours(@NotNull @Valid ShiftHoursDTO shiftHoursDTO);
    int[] getShiftAsArray(@NotNull @Valid ShiftHoursDTO shiftHoursDTO);
}
