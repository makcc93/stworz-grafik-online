package online.stworzgrafik.StworzGrafik.employee.workNorm;

import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.CreateSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.ResponseSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.UpdateSpecialWorkNormDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpecialWorkNormMapper {

    ResponseSpecialWorkNormDTO toDto(SpecialWorkNorm norm);

    List<ResponseSpecialWorkNormDTO> toDtoList(List<SpecialWorkNorm> norms);

    SpecialWorkNorm toEntity(CreateSpecialWorkNormDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateSpecialWorkNormDTO dto, @MappingTarget SpecialWorkNorm norm);
}
