package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;

import java.util.List;

public interface RegionService {
    ResponseRegionDTO createRegion(CreateRegionDTO createRegionDTO);
    ResponseRegionDTO updateRegion(Long id, UpdateRegionDTO updateRegionDTO);
    List<ResponseRegionDTO> findAll();
    ResponseRegionDTO findById(Long id);
    boolean exists(Long id);
    boolean exists(String name);
    void deleteRegion(Long id);
}
