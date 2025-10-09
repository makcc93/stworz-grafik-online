package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PositionServiceImpl implements PositionService{
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;
    private final PositionBuilder positionBuilder;

    public PositionServiceImpl(PositionRepository positionRepository, PositionMapper positionMapper, PositionBuilder positionBuilder) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
        this.positionBuilder = positionBuilder;
    }

    @Override
    public List<ResponsePositionDTO> findAll() {
        return positionRepository.findAll().stream()
                .map(positionMapper::toResponsePositionDTO)
                .toList();
    }

    @Override
    public ResponsePositionDTO findById(Long id) {
        ArgumentNullChecker.check(id, "Id");

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));

        return positionMapper.toResponsePositionDTO(position);
    }

    @Override
    public ResponsePositionDTO createPosition(CreatePositionDTO createPositionDTO) {
        ArgumentNullChecker.check(createPositionDTO);

        if (positionRepository.existsByName(createPositionDTO.name())){
            throw new EntityExistsException("Position with name " + createPositionDTO.name() + " already exists");
        }

        Position position = positionBuilder.createPosition(createPositionDTO.name(), createPositionDTO.description());
        Position savedPosition = positionRepository.save(position);

        return positionMapper.toResponsePositionDTO(savedPosition);
    }

    @Override
    public ResponsePositionDTO updatePosition(Long id, UpdatePositionDTO updatePositionDTO) {
        ArgumentNullChecker.check(id, "Id");
        ArgumentNullChecker.check(updatePositionDTO);

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + id));

        positionMapper.updatePosition(updatePositionDTO,position);

        return positionMapper.toResponsePositionDTO(position);
    }

    @Override
    public void deletePosition(Long id) {
        ArgumentNullChecker.check(id, "Id");

        if (!positionRepository.existsById(id)){
            throw new EntityNotFoundException("Position with id " + id + " does not exist");
        }

        positionRepository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        ArgumentNullChecker.check(id,"Id");

        return positionRepository.existsById(id);
    }

    @Override
    public boolean exists(String name) {
        ArgumentNullChecker.check(name,"Name");

        return positionRepository.existsByName(name);
    }
}
