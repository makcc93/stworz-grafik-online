package online.stworzgrafik.StworzGrafik.region;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;

import java.util.List;

public interface RegionService {
    ResponseRegionDTO createRegion(@NotNull CreateRegionDTO createRegionDTO);
    ResponseRegionDTO updateRegion(@NotNull Long id, @NotNull UpdateRegionDTO updateRegionDTO);
    List<ResponseRegionDTO> findAll();
    ResponseRegionDTO findById(@NotNull Long id);
    boolean exists(@NotNull Long id);
    boolean exists(@NotNull String name);
    void deleteRegion(@NotNull Long id);
}
