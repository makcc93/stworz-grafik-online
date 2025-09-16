package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
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
        ArgumentNullChecker.check(shiftHoursDTO,"ShiftHoursDTO");

        Shift shift = shiftMapper.toEntity(shiftHoursDTO);

        Shift savedShift = saveEntity(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    @Override
    public Shift saveEntity(Shift shift) {
        ArgumentNullChecker.check(shift,"Shift");

        return shiftRepository.save(shift);
    }


    @Override
    public ResponseShiftDTO create(ShiftHoursDTO shiftHoursDTO) {
        ArgumentNullChecker.check(shiftHoursDTO);

        Shift shift = shiftBuilder.createShift(
                shiftHoursDTO.startHour(),
                shiftHoursDTO.endHour()
        );

        Shift savedShift = shiftRepository.save(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    @Override
    public Shift findEntityById(Long id) {
        ArgumentNullChecker.check(id,"Id");

        return shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));
    }

    @Override
    public void delete(Long id) {
        ArgumentNullChecker.check(id,"Id");

        if (!exists(id)){
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
        ArgumentNullChecker.check(id,"Id");

        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));

        return shiftMapper.toShiftDto(shift);
    }

    @Override
    public boolean exists(LocalTime startHour, LocalTime endHour) {
        ArgumentNullChecker.check(startHour,"Start hour");
        ArgumentNullChecker.check(endHour,"End hour");

        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }

        return shiftRepository.existsByStartHourAndEndHour(startHour,endHour);
    }

    @Override
    public boolean exists(Long id) {
        ArgumentNullChecker.check(id,"Id");

        return shiftRepository.existsById(id);
    }
}
