package online.stworzgrafik.StworzGrafik.employee.workNorm;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.CreateSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.ResponseSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.UpdateSpecialWorkNormDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialWorkNormServiceImpl implements SpecialWorkNormService, SpecialWorkNormEntityService {

    private final SpecialWorkNormRepository repository;
    private final SpecialWorkNormMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ResponseSpecialWorkNormDTO> findAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseSpecialWorkNormDTO> findAllActive() {
        return mapper.toDtoList(repository.findAllByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseSpecialWorkNormDTO findById(Long id) {
        return mapper.toDto(getEntityById(id));
    }

    @Override
    @Transactional
    public ResponseSpecialWorkNormDTO create(CreateSpecialWorkNormDTO dto) {
        if (repository.existsByName(dto.name())) {
            throw new IllegalArgumentException("SpecialWorkNorm with name '" + dto.name() + "' already exists");
        }

        SpecialWorkNorm norm = mapper.toEntity(dto);
        norm.setActive(true);
        SpecialWorkNorm saved = repository.save(norm);

        log.info("Created SpecialWorkNorm id={} name={}", saved.getId(), saved.getName());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ResponseSpecialWorkNormDTO update(Long id, UpdateSpecialWorkNormDTO dto) {
        SpecialWorkNorm norm = getEntityById(id);
        mapper.update(dto, norm);
        SpecialWorkNorm saved = repository.save(norm);

        log.info("Updated SpecialWorkNorm id={}", id);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SpecialWorkNorm norm = getEntityById(id);
        repository.delete(norm);
        log.info("Deleted SpecialWorkNorm id={}", id);
    }

    @Override
    public SpecialWorkNorm getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SpecialWorkNorm not found: " + id));
    }
}
