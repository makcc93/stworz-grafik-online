package online.stworzgrafik.StworzGrafik.region;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.RegionSpecificationDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface RegionService {
     ResponseRegionDTO createRegion(@NotNull @Valid CreateRegionDTO createRegionDTO);
     ResponseRegionDTO updateRegion(@NotNull Long id, @NotNull @Valid UpdateRegionDTO updateRegionDTO);
     List<ResponseRegionDTO> findAll();
     ResponseRegionDTO findById(@NotNull Long id);
     List<ResponseRegionDTO> findByCriteria(@Nullable RegionSpecificationDTO dto);
     ResponseRegionDTO save(@NotNull Region region);
     boolean exists(@NotNull Long id);
     boolean exists(@NotNull String name);
     void delete(@NotNull Long id);

}
