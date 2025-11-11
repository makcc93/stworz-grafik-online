package online.stworzgrafik.StworzGrafik.region;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface RegionService {
    public ResponseRegionDTO createRegion(@NotNull @Valid CreateRegionDTO createRegionDTO);
    public ResponseRegionDTO updateRegion(@NotNull Long id, @NotNull @Valid UpdateRegionDTO updateRegionDTO);
    public List<ResponseRegionDTO> findAll();
    public ResponseRegionDTO findById(@NotNull Long id);
    public ResponseRegionDTO save(@NotNull Region region);
    public boolean exists(@NotNull Long id);
    public boolean exists(@NotNull String name);
    public void delete(@NotNull Long id);

}
