package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class ShiftServiceImpl implements ShiftService{
    private final ShiftRepository shiftRepository;
    private final ShiftBuilder shiftBuilder;
    private final ShiftMapper shiftMapper;

    public ShiftServiceImpl(ShiftRepository shiftRepository, ShiftBuilder shiftBuilder, ShiftMapper shiftMapper) {
        this.shiftRepository = shiftRepository;
        this.shiftBuilder = shiftBuilder;
        this.shiftMapper = shiftMapper;
    }

    @Override
    public ResponseShiftDTO saveDto(ShiftHoursDTO shiftHoursDTO) {
        Objects.requireNonNull(shiftHoursDTO);

        validateHour(shiftHoursDTO.startHour(),shiftHoursDTO.endHour());

        Shift shift = shiftMapper.toEntity(shiftHoursDTO);

        Shift savedShift = saveEntity(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    @Override
    public Shift saveEntity(Shift shift) {
       Objects.requireNonNull(shift,"Shift cannot be null");

       validateHour(shift.startHour,shift.endHour);

        return shiftRepository.save(shift);
    }


    @Override
    public ResponseShiftDTO create(ShiftHoursDTO shiftHoursDTO) {
        Objects.requireNonNull(shiftHoursDTO);

        validateHour(shiftHoursDTO.startHour(),shiftHoursDTO.endHour());

        Shift shift = shiftBuilder.createShift(
                shiftHoursDTO.startHour(),
                shiftHoursDTO.endHour()
        );

        Shift savedShift = shiftRepository.save(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    @Override
    public Shift findEntityById(Long id) {
       Objects.requireNonNull(id,"Id cannot be null");

        return shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));
    }

    @Override
    public void delete(Long id) {
        Objects.requireNonNull(id, "Id cannot be null");

        if (!shiftRepository.existsById(id)){
            throw new EntityNotFoundException("Shift with id " + id +" does not exist");
        }

        shiftRepository.deleteById(id);
    }

    @Override
    public List<ResponseShiftDTO> findAll() {
        List<Shift> shifts = shiftRepository.findAll();

        return shifts.stream()
                .map(shiftMapper::toShiftDto)
                .toList();
    }

    @Override
    public ResponseShiftDTO findById(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));

        return shiftMapper.toShiftDto(shift);
    }

    @Override
    public boolean exists(LocalTime startHour, LocalTime endHour) {
        Objects.requireNonNull(startHour,"Start hour cannot be null");
        Objects.requireNonNull(endHour,"End hour cannot be null");

        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }

        return shiftRepository.existsByStartHourAndEndHour(startHour,endHour);
    }

    @Override
    public boolean exists(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        return shiftRepository.existsById(id);
    }

    private void validateHour(LocalTime startHour, LocalTime endHour){
        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }
    }
}
