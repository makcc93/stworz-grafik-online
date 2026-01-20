package online.stworzgrafik.StworzGrafik.region;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.RegionSpecificationDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
class RegionServiceImpl implements RegionService, RegionEntityService {
    private final RegionRepository regionRepository;
    private final RegionBuilder regionBuilder;
    private final RegionMapper regionMapper;
    private final NameValidatorService nameValidatorService;

    @Override
    public ResponseRegionDTO createRegion(CreateRegionDTO createRegionDTO) {
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
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));

        return regionMapper.toResponseRegionDTO(region);
    }

    @Override
    public List<ResponseRegionDTO> findByCriteria(@Nullable RegionSpecificationDTO dto) {
        if (dto == null) return Collections.emptyList();

        Specification<Region> specification = Specification.allOf(
                RegionSpecification.hasId(dto.id()),
                RegionSpecification.hasNameLike(dto.name()),
                RegionSpecification.isEnable(dto.enable())
        );

        return regionRepository.findAll(specification).stream()
                .map(regionMapper::toResponseRegionDTO)
                .toList();
    }

    @Override
    public ResponseRegionDTO save(Region region) {
        Region savedRegion = regionRepository.save(region);

        return regionMapper.toResponseRegionDTO(savedRegion);
    }

    @Override
    public boolean exists(Long id) {
        return regionRepository.existsById(id);
    }

    @Override
    public boolean exists(String name) {
        return regionRepository.existsByName(name);
    }

    @Override
    public void delete(Long id) {
        if (!regionRepository.existsById(id)){
            throw new EntityNotFoundException("Cannot find region by id " + id);
        }

        regionRepository.deleteById(id);
    }

    @Override
    public Region saveEntity(Region region) {
        return regionRepository.save(region);
    }

    @Override
    public Region getEntityById(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));
    }
}
