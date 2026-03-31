package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftCriteriaDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        checkNull(shift.getStartHour(), shift.getEndHour());

        if (shiftRepository.existsByStartHourAndEndHour(shift.getStartHour(), shift.getEndHour())){
            return shiftMapper.toShiftDto(shift);
        }

        Shift savedShift = shiftRepository.save(shift);

        return shiftMapper.toShiftDto(savedShift);
    }

    @Override
    public ResponseShiftDTO create(ShiftHoursDTO shiftHoursDTO) {
        LocalTime startHour = shiftHoursDTO.startHour();
        LocalTime endHour = shiftHoursDTO.endHour();

        checkNull(startHour, endHour);

        if (shiftRepository.existsByStartHourAndEndHour(startHour, endHour)){
            return shiftMapper.toShiftDto(
                    shiftRepository.findByStartHourAndEndHour(
                            startHour,
                            endHour)
                    .orElseThrow());
        }

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
    public ResponseShiftDTO findById(Long id) {
        Shift shift = shiftRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));

        return shiftMapper.toShiftDto(shift);
    }

    @Override
    public Page<ResponseShiftDTO> findByCriteria(ShiftCriteriaDTO dto, Pageable pageable) {
        Specification<Shift> specification = Specification.allOf(
                ShiftSpecification.hasStartHour(dto.startHour()),
                ShiftSpecification.hasEndHour(dto.endHour())
        );

        return shiftRepository.findAll(specification,pageable)
                .map(shiftMapper::toShiftDto);
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
    public Integer getLength(ShiftHoursDTO shiftHoursDTO) {
        checkNull(shiftHoursDTO.startHour(),shiftHoursDTO.endHour());

        return shiftHoursDTO.endHour().getHour() - shiftHoursDTO.startHour().getHour();
    }

    @Override
    public BigDecimal getDurationHours(ShiftHoursDTO shiftHoursDTO) {
        checkNull(shiftHoursDTO.startHour(),shiftHoursDTO.endHour());

        int length = shiftHoursDTO.endHour().getHour() - shiftHoursDTO.startHour().getHour();

        return BigDecimal.valueOf(length);
    }

    @Override
    public int[] getShiftAsArray(ShiftHoursDTO shiftHoursDTO) {
        checkNull(shiftHoursDTO.startHour(),shiftHoursDTO.endHour());

        int[] array = new int[24];

        for (int hour = shiftHoursDTO.startHour().getHour(); hour <= shiftHoursDTO.endHour().getHour(); hour++){
            array[hour] = 1;
        }

        return array;
    }

    @Override
    public Shift saveEntity(Shift shift) {
        if (shiftRepository.existsByStartHourAndEndHour(shift.getStartHour(), shift.getEndHour())){
            return shift;
        }

        checkNull(shift.getStartHour(), shift.getEndHour());

        return shiftRepository.save(shift);
    }

    @Override
    public void saveAll(List<Shift> shifts) {
        shiftRepository.saveAll(shifts);
    }

    @Override
    public Shift getEntityById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find shift by id: " + id));
    }

    @Override
    public Shift getEntityByHours(LocalTime startHour, LocalTime endHour) {
        return shiftRepository.findByStartHourAndEndHour(startHour,endHour)
                .orElseGet(() -> shiftMapper.toEntity(create(new ShiftHoursDTO(startHour,endHour))));
    }

    @Override
    public Shift getArrayAsShift(int[] array) {
        if (array.length != 24){
            throw new IllegalArgumentException("Shift array must equal 24 elements");
        }

        int startHour = 0;
        int endHour = 0;

        for (int i = 0; i < array.length; i++){
            if (array[i] != 0){
                startHour = array[i];
                break;
            }
        }

        for (int i = 23; i >= 0; i--){
            if (array[i] != 0){
                endHour = array[i];
                break;
            }
        }

        return shiftMapper.toEntity(create(new ShiftHoursDTO(LocalTime.of(startHour,0),LocalTime.of(endHour,0))));
    }

    @Override
    public int[] getShiftAsArray(Shift shift) {
        checkNull(shift.getStartHour(),shift.getEndHour());

        int[] array = new int[24];

        for (int hour = shift.getStartHour().getHour(); hour <= shift.getEndHour().getHour(); hour++){
            array[hour] = 1;
        }

        return array;
    }

    @Override
    public Shift updateShift(ShiftHoursDTO dto,Shift shift) {
        shiftMapper.updateShift(dto,shift);
        return  shiftRepository.save(shift);
    }

    @Override
    public List<Shift> getAll() {
        return shiftRepository.findAll();
    }

    private void checkNull(LocalTime startHour, LocalTime endHour){
        if (startHour == null || endHour == null){
            throw new IllegalArgumentException("Start or end hour cannot be null");
        }
    }
}
