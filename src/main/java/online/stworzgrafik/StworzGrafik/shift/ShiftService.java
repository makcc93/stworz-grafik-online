package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;

import java.time.LocalTime;
import java.util.List;

public interface ShiftService {
    ResponseShiftDTO saveDto(ShiftHoursDTO shiftHoursDTO);
    Shift saveEntity(Shift shift);
    ResponseShiftDTO create(ShiftHoursDTO shiftHoursDTO);
    Shift findEntityById(Long id);
    void delete(Long id);
    List<ResponseShiftDTO> findAll();
    ResponseShiftDTO findById(Long id);
    boolean exists(LocalTime startHour, LocalTime endHour);
    boolean exists(Long id);

}
