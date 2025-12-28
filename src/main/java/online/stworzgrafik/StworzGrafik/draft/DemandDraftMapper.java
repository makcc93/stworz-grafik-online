package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface DemandDraftMapper {
    @Mapping(source = "store.id", target = "store_id")
    ResponseDemandDraftDTO toResponseDemandDraftDTO(DemandDraft demandDraft);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDemandDraft(UpdateDemandDraftDTO updateDemandDraftDTO, @MappingTarget DemandDraft demandDraft);
}