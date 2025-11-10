package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
class RegionServiceImpl implements RegionService, RegionEntityService {
    private final RegionRepository regionRepository;
    private final RegionBuilder regionBuilder;
    private final RegionMapper regionMapper;
    private final NameValidatorService nameValidatorService;

    @Override
    public ResponseRegionDTO createRegion(@Valid CreateRegionDTO createRegionDTO) {
        if (regionRepository.existsByName(createRegionDTO.name())){
            throw new EntityExistsException("Region with name " + createRegionDTO.name() + " already exist");
        }

        String validatedName = nameValidatorService.validate(createRegionDTO.name(), ObjectType.REGION);

        Region region = regionBuilder.createRegion(validatedName);

        Region savedRegion = regionRepository.save(region);

        return regionMapper.toResponseRegionDTO(savedRegion);
    }

    @Override
    public ResponseRegionDTO updateRegion(@Valid Long id,@Valid UpdateRegionDTO updateRegionDTO) {
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
    public ResponseRegionDTO findById(@Valid Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));

        return regionMapper.toResponseRegionDTO(region);
    }

    @Override
    public ResponseRegionDTO save(@Valid Region region) {
        Region savedRegion = regionRepository.save(region);

        return regionMapper.toResponseRegionDTO(savedRegion);
    }

    @Override
    public boolean exists(@Valid Long id) {
        return regionRepository.existsById(id);
    }

    @Override
    public boolean exists(@Valid String name) {
        return regionRepository.existsByName(name);
    }

    @Override
    public void delete(@Valid Long id) {
        if (!regionRepository.existsById(id)){
            throw new EntityNotFoundException("Cannot find region by id " + id);
        }

        regionRepository.deleteById(id);
    }

    @Override
    public Region saveEntity(@Valid Region region) {
        return regionRepository.save(region);
    }

    @Override
    public Region getEntityById(@Valid Long id) {
        return null;
    }
}
