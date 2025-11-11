package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
class ShiftServiceImpl implements ShiftService, ShiftEntityService{
    private final ShiftRepository shiftRepository;
    private final ShiftBuilder shiftBuilder;
    private final ShiftMapper shiftMapper;

    @Override
    public ResponseShiftDTO save(Shift shift) {
        validateHour(shift.getStartHour(),shift.getEndHour());

        Shift savedShift = saveEntity(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    @Override
    public ResponseShiftDTO create(ShiftHoursDTO shiftHoursDTO) {
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

    @Override
    public ResponseShiftDTO updateShift(Long shiftId, ShiftHoursDTO shiftHoursDTO){
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id " + shiftId));

        shiftMapper.updateShift(shiftHoursDTO,shift);
        shiftRepository.save(shift);

        return shiftMapper.toShiftDto(shift);
    }

    @Override
    public void delete(Long id) {
        if (!shiftRepository.existsById(id)){
            throw new EntityNotFoundException("Shift with id " + id +" does not exist");
        }

        shiftRepository.deleteById(id);
    }

    @Override
    public List<ResponseShiftDTO> findAll() {
        return shiftRepository.findAll().stream()
                .map(shiftMapper::toShiftDto)
                .toList();
    }

    @Override
    public ResponseShiftDTO findById(Long id) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));

        return shiftMapper.toShiftDto(shift);
    }

    @Override
    public boolean exists(LocalTime startHour, LocalTime endHour) {
        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }

        return shiftRepository.existsByStartHourAndEndHour(startHour,endHour);
    }

    @Override
    public boolean exists(Long id) {
        return shiftRepository.existsById(id);
    }

    @Override
    public Shift saveEntity(Shift shift) {
        if (shiftRepository.existsByStartHourAndEndHour(shift.startHour,shift.endHour)){
            return shift;
        }

        validateHour(shift.startHour,shift.endHour);

        return shiftRepository.save(shift);
    }

    @Override
    public Shift getEntityById(Long id) {
        return shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));
    }

    private void validateHour(LocalTime startHour, LocalTime endHour){
        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }
    }
}
