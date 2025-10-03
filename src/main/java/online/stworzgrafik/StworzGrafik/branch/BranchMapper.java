package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import org.hibernate.dialect.function.array.OracleArrayGetFunction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    @Mapping(source = "region.id", target = "regionId")
    @Mapping(source = "region.name", target = "regionName")
    ResponseBranchDTO toResponseBranchDTO(Branch branch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBranchFromDTO(UpdateBranchDTO updateBranchDTO, @MappingTarget Branch branch);
}