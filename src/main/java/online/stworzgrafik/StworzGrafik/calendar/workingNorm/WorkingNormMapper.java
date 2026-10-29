package online.stworzgrafik.StworzGrafik.calendar.workingNorm;

import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormRequest;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE )
public interface WorkingNormMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(WorkingNormRequest dto, @MappingTarget WorkingNorm workingNorm);

    WorkingNormResponse toResponseDTO(WorkingNorm workingNorm);
}
