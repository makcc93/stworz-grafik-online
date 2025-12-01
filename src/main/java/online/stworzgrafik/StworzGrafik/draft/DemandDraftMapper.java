package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
interface DemandDraftMapper {
    ResponseDemandDraftDTO toResponseDemandDraftDTO(DemandDraft demandDraft);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDemandDraft(UpdateDemandDraftDTO updateDemandDraftDTO, @MappingTarget DemandDraft demandDraft);
}