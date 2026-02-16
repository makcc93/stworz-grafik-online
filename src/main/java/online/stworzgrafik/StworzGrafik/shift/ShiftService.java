package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftCriteriaDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Validated
public interface ShiftService {
    ResponseShiftDTO save(@NotNull Shift shift);
    ResponseShiftDTO create(@NotNull ShiftHoursDTO shiftHoursDTO);
    ResponseShiftDTO updateShift(@NotNull Long shiftId, ShiftHoursDTO shiftHoursDTO);
    void delete(@NotNull Long id);
    ResponseShiftDTO findById(@NotNull Long id);
    Page<ResponseShiftDTO> findByCriteria(ShiftCriteriaDTO dto,Pageable pageable);
    boolean exists(@NotNull LocalTime startHour, @NotNull LocalTime endHour);
    boolean exists(@NotNull Long id);
    Integer getLength(@NotNull @Valid ShiftHoursDTO shiftHoursDTO);
    BigDecimal getDurationHours(@NotNull ShiftHoursDTO shiftHoursDTO);
    int[] getShiftAsArray(@NotNull ShiftHoursDTO shiftHoursDTO);
}
