package online.stworzgrafik.StworzGrafik.draft;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;

@RequiredArgsConstructor
public class DemandDraftServiceImpl implements DemandDraftService, DemandDraftEntityService{
    private final DemandDraftRepository demandDraftRepository;
    private final StoreEntityService storeEntityService;
    private final DemandDraftMapper demandDraftMapper;

    @Override
    public ResponseDemandDraftDTO createDemandDraft(CreateDemandDraftDTO createDemandDraftDTO) {
        Store store = storeEntityService.getEntityById(createDemandDraftDTO.storeId());
        Integer year = createDemandDraftDTO.year();
        Integer month = createDemandDraftDTO.month();
        Integer day = createDemandDraftDTO.day();
        int[] hourlyDemand = createDemandDraftDTO.hourlyDemand();

        DemandDraft demandDraft = new DemandDraftBuilder().createDemandDraft(
                store,
                year,
                month,
                day,
                hourlyDemand
        );

        DemandDraft savedDemandDraft = demandDraftRepository.save(demandDraft);

        return demandDraftMapper.toResponseDemandDraftDTO(savedDemandDraft);
    }

    @Override
    public DemandDraft saveEntity(DemandDraft demandDraft) {
        return null;
    }

    @Override
    public DemandDraft getEntityById(Long id) {
        return null;
    }
}
