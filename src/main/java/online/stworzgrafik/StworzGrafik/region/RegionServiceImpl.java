package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
public class RegionServiceImpl implements RegionService{
    private final RegionRepository regionRepository;
    private final RegionBuilder regionBuilder;
    private final RegionMapper regionMapper;
    private final NameValidatorService nameValidatorService;

    public RegionServiceImpl(RegionRepository regionRepository, RegionBuilder regionBuilder, RegionMapper regionMapper, NameValidatorService nameValidatorService) {
        this.regionRepository = regionRepository;
        this.regionBuilder = regionBuilder;
        this.regionMapper = regionMapper;
        this.nameValidatorService = nameValidatorService;
    }

    @Override
    public ResponseRegionDTO createRegion(CreateRegionDTO createRegionDTO) {
        Objects.requireNonNull(createRegionDTO);

        if (regionRepository.existsByName(createRegionDTO.name())){
            throw new EntityExistsException("Region with name " + createRegionDTO.name() + " already exist");
        }

        String validatedName = nameValidatorService.validate(createRegionDTO.name(), ObjectType.REGION);

        Region region = regionBuilder.createRegion(validatedName);

        Region savedRegion = regionRepository.save(region);

        return regionMapper.toResponseRegionDTO(savedRegion);
    }

    @Override
    public ResponseRegionDTO updateRegion(Long id, UpdateRegionDTO updateRegionDTO) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(updateRegionDTO);

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));

        if (updateRegionDTO.name() != null) {
            String validatedName = nameValidatorService.validate(updateRegionDTO.name(), ObjectType.REGION);
            region.setName(validatedName);
        }

        regionMapper.updateRegionFromDTO(updateRegionDTO,region);

        Region savedRegion = regionRepository.save(region);

        return regionMapper.toResponseRegionDTO(savedRegion);
    }

    @Override
    public List<ResponseRegionDTO> findAll() {
        return regionRepository.findAll().stream()
                .map(regionMapper::toResponseRegionDTO)
                .toList();
    }

    @Override
    public ResponseRegionDTO findById(Long id) {
        Objects.requireNonNull(id, "Id cannot be null");

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));

        return regionMapper.toResponseRegionDTO(region);
    }

    @Override
    public boolean exists(Long id) {
        Objects.requireNonNull(id, "Id cannot be null");

        return regionRepository.existsById(id);
    }

    @Override
    public boolean exists(String name) {
        Objects.requireNonNull(name, "Name cannot be null");

        return regionRepository.existsByName(name);
    }

    @Override
    public void deleteRegion(Long id) {
        Objects.requireNonNull(id, "Id cannot be null");

        if (!regionRepository.existsById(id)){
            throw new EntityNotFoundException("Cannot find region by id " + id);
        }

        regionRepository.deleteById(id);
    }
}
