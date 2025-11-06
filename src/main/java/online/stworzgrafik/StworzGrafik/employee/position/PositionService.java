package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
public class PositionService{
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final PositionBuilder positionBuilder;
    private final NameValidatorService nameValidatorService;

    public PositionService(PositionRepository positionRepository, PositionMapper positionMapper, PositionBuilder positionBuilder, NameValidatorService nameValidatorService) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
        this.positionBuilder = positionBuilder;
        this.nameValidatorService = nameValidatorService;
    }

    public List<ResponsePositionDTO> findAll() {
        return positionRepository.findAll().stream()
                .map(positionMapper::toResponsePositionDTO)
                .toList();
    }

    public ResponsePositionDTO findById(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));

        return positionMapper.toResponsePositionDTO(position);
    }

    public ResponsePositionDTO createPosition(CreatePositionDTO createPositionDTO) {
        Objects.requireNonNull(createPositionDTO);

        if (positionRepository.existsByName(createPositionDTO.name())){
            throw new EntityExistsException("Position with name " + createPositionDTO.name() + " already exists");
        }

        String validatedName = nameValidatorService.validate(createPositionDTO.name(), ObjectType.POSITION);

        Position position = positionBuilder.createPosition(validatedName, createPositionDTO.description());
        Position savedPosition = positionRepository.save(position);

        return positionMapper.toResponsePositionDTO(savedPosition);
    }

    public ResponsePositionDTO updatePosition(Long id, UpdatePositionDTO updatePositionDTO) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(updatePositionDTO);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));

        if (updatePositionDTO.name() != null){
            String validatedName = nameValidatorService.validate(updatePositionDTO.name(), ObjectType.POSITION);
            position.setName(validatedName);
        }

        positionMapper.updatePosition(updatePositionDTO,position);

        return positionMapper.toResponsePositionDTO(position);
    }

    public void deletePosition(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        if (!positionRepository.existsById(id)){
            throw new EntityNotFoundException("Position with id " + id + " does not exist");
        }

        positionRepository.deleteById(id);
    }

    public boolean exists(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        return positionRepository.existsById(id);
    }

    public boolean exists(String name) {
        Objects.requireNonNull(name,"Name cannot be null");

        return positionRepository.existsByName(name);
    }
}
