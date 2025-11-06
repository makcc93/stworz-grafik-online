package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface RegionMapper {

    ResponseRegionDTO toResponseRegionDTO(Region region);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRegionFromDTO(UpdateRegionDTO updateRegionDTO, @MappingTarget Region region);
}
