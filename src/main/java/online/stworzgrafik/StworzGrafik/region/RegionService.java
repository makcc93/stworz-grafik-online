package online.stworzgrafik.StworzGrafik.region;

import jakarta.validation.Valid;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface RegionService {
    public ResponseRegionDTO createRegion(@Valid CreateRegionDTO createRegionDTO);
    public ResponseRegionDTO updateRegion(@Valid Long id,@Valid UpdateRegionDTO updateRegionDTO);
    public List<ResponseRegionDTO> findAll();
    public ResponseRegionDTO findById(@Valid Long id);
    public ResponseRegionDTO save(@Valid Region region);
    public boolean exists(@Valid Long id);
    public boolean exists(@Valid String name);
    public void delete(@Valid Long id);

}
