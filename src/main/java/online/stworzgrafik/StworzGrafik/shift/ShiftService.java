package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@Validated
@AllArgsConstructor
public class ShiftService{
    private final ShiftRepository shiftRepository;
    private final ShiftBuilder shiftBuilder;
    private final ShiftMapper shiftMapper;

    public ResponseShiftDTO saveDto(@Valid ShiftHoursDTO shiftHoursDTO) {
        validateHour(shiftHoursDTO.startHour(),shiftHoursDTO.endHour());

        Shift shift = shiftMapper.toEntity(shiftHoursDTO);

        Shift savedShift = saveEntity(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    public Shift saveEntity(@Valid Shift shift) {
       if (shiftRepository.existsByStartHourAndEndHour(shift.startHour,shift.endHour)){
           return shift;
       }

       validateHour(shift.startHour,shift.endHour);

       return shiftRepository.save(shift);
    }

    public ResponseShiftDTO create(@Valid ShiftHoursDTO shiftHoursDTO) {
        LocalTime startHour = shiftHoursDTO.startHour();
        LocalTime endHour = shiftHoursDTO.endHour();

        if (shiftRepository.existsByStartHourAndEndHour(startHour, endHour)){
            return shiftMapper.toShiftDto(
                    shiftRepository.findByStartHourAndEndHour(
                            startHour,
                            endHour)
                    .orElseThrow());
        }

        validateHour(startHour, endHour);

        Shift shift = shiftBuilder.createShift(
                startHour,
                endHour
        );

        Shift savedShift = shiftRepository.save(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    public ResponseShiftDTO updateShift(@Valid Long shiftId, @Valid ShiftHoursDTO shiftHoursDTO){
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id " + shiftId));

        shiftMapper.updateShift(shiftHoursDTO,shift);
        shiftRepository.save(shift);

        return shiftMapper.toShiftDto(shift);
    }

    public Shift findEntityById(@Valid Long id) {
        return shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));
    }

    public void delete(@Valid Long id) {
        if (!shiftRepository.existsById(id)){
            throw new EntityNotFoundException("Shift with id " + id +" does not exist");
        }

        shiftRepository.deleteById(id);
    }

    public List<ResponseShiftDTO> findAll() {
        return shiftRepository.findAll().stream()
                .map(shiftMapper::toShiftDto)
                .toList();
    }

    public ResponseShiftDTO findById(@Valid Long id) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));

        return shiftMapper.toShiftDto(shift);
    }

    public boolean exists(@Valid LocalTime startHour, @Valid LocalTime endHour) {
        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }

        return shiftRepository.existsByStartHourAndEndHour(startHour,endHour);
    }

    public boolean exists(@Valid Long id) {
        return shiftRepository.existsById(id);
    }

    private void validateHour(@Valid LocalTime startHour, @Valid LocalTime endHour){
        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }
    }
}
