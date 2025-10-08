package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;

import java.util.List;

public class PositionServiceImpl implements PositionService{
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    public PositionServiceImpl(PositionRepository positionRepository, PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
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
        return null;
    }

    @Override
    public ResponsePositionDTO updatePosition(UpdatePositionDTO updatePositionDTO) {
        return null;
    }

    @Override
    public void deletePosition(Long id) {

    }

    @Override
    public boolean exists(Long id) {
        return false;
    }

    @Override
    public boolean exists(String name) {
        return false;
    }
}
