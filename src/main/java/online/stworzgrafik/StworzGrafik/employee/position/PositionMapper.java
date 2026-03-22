package online.stworzgrafik.StworzGrafik.employee.position;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface PositionMapper {

    ResponsePositionDTO toResponsePositionDTO(Position position);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePosition(UpdatePositionDTO updatePositionDTO, @MappingTarget Position position);
}
