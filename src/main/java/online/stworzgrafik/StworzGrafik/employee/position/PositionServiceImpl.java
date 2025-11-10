package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
class PositionServiceImpl implements PositionService, PositionEntityService{
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final PositionBuilder positionBuilder;
    private final NameValidatorService nameValidatorService;

    @Override
    public List<ResponsePositionDTO> findAll() {
        return positionRepository.findAll().stream()
                .map(positionMapper::toResponsePositionDTO)
                .toList();
    }

    @Override
    public ResponsePositionDTO findById(@Valid Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));

        return positionMapper.toResponsePositionDTO(position);
    }

    @Override
    public ResponsePositionDTO createPosition(@Valid CreatePositionDTO createPositionDTO) {
        if (positionRepository.existsByName(createPositionDTO.name())){
            throw new EntityExistsException("Position with name " + createPositionDTO.name() + " already exists");
        }

        String validatedName = nameValidatorService.validate(createPositionDTO.name(), ObjectType.POSITION);

        Position position = positionBuilder.createPosition(validatedName, createPositionDTO.description());
        Position savedPosition = positionRepository.save(position);

        return positionMapper.toResponsePositionDTO(savedPosition);
    }

    @Override
    public ResponsePositionDTO updatePosition(@Valid Long id, @Valid UpdatePositionDTO updatePositionDTO) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));

        if (updatePositionDTO.name() != null){
            String validatedName = nameValidatorService.validate(updatePositionDTO.name(), ObjectType.POSITION);
            position.setName(validatedName);
        }

        positionMapper.updatePosition(updatePositionDTO,position);

        return positionMapper.toResponsePositionDTO(position);
    }

    @Override
    public ResponsePositionDTO save(@Valid Position position){
        Position savedPosition = positionRepository.save(position);

        return positionMapper.toResponsePositionDTO(savedPosition);
    }

    @Override
    public void delete(@Valid Long id) {
        if (!positionRepository.existsById(id)){
            throw new EntityNotFoundException("Position with id " + id + " does not exist");
        }

        positionRepository.deleteById(id);
    }

    @Override
    public boolean exists(@Valid Long id) {
        return positionRepository.existsById(id);
    }

    @Override
    public boolean exists(@Valid String name) {
        return positionRepository.existsByName(name);
    }

    @Override
    public Position saveEntity(@Valid Position position) {
        return positionRepository.save(position);
    }

    @Override
    public Position getEntityById(@Valid Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));
    }
}
