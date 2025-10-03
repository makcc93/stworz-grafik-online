package online.stworzgrafik.StworzGrafik.region;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionServiceImpl implements RegionService{
    private final RegionRepository regionRepository;
    private final RegionBuilder regionBuilder;
    private final RegionMapper regionMapper;

    public RegionServiceImpl(RegionRepository regionRepository, RegionBuilder regionBuilder, RegionMapper regionMapper) {
        this.regionRepository = regionRepository;
        this.regionBuilder = regionBuilder;
        this.regionMapper = regionMapper;
    }

    @Override
    public ResponseRegionDTO createRegion(CreateRegionDTO createRegionDTO) {
        ArgumentNullChecker.check(createRegionDTO);

        if (regionRepository.existsByName(createRegionDTO.name())){
            throw new EntityExistsException("Region with name " + createRegionDTO.name() + " already exist");
        }

        Region region = regionBuilder.createRegion(createRegionDTO.name());

        Region savedRegion = regionRepository.save(region);

        return regionMapper.toResponseRegionDTO(savedRegion);
    }

    @Override
    public ResponseRegionDTO updateRegion(Long id, UpdateRegionDTO updateRegionDTO) {
        ArgumentNullChecker.check(id,"Id");
        ArgumentNullChecker.check(updateRegionDTO);

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));

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
        ArgumentNullChecker.check(id,"Id");

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + id));

        return regionMapper.toResponseRegionDTO(region);
    }

    @Override
    public boolean exists(Long id) {
        ArgumentNullChecker.check(id,"Id");

        return regionRepository.existsById(id);
    }

    @Override
    public boolean exists(String name) {
        ArgumentNullChecker.check(name,"Name");

        return regionRepository.existsByName(name);
    }

    @Override
    public void deleteRegion(Long id) {
        ArgumentNullChecker.check(id,"Id");

        if (!regionRepository.existsById(id)){
            throw new EntityNotFoundException("Cannot find region by id " + id);
        }

        regionRepository.deleteById(id);
    }
}
