package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.StoreAccurateDayDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;

import java.util.List;

@RequiredArgsConstructor
class DemandDraftServiceImpl implements DemandDraftService, DemandDraftEntityService{
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

        DemandDraft demandDraft = demandDraftRepository.findByStoreAndYearAndMonthAndDay(store, year, month, day)
                    .orElseGet(() ->
                            new DemandDraftBuilder().createDemandDraft(
                                store,
                                year,
                                month,
                                day,
                                hourlyDemand
                            )
                    );

        if (demandDraft.getId() != null){
            demandDraft.setHourlyDemand(hourlyDemand);
        }

        DemandDraft savedDemandDraft = demandDraftRepository.save(demandDraft);

        return demandDraftMapper.toResponseDemandDraftDTO(savedDemandDraft);
    }

    @Override
    public ResponseDemandDraftDTO updateDemandDraft(UpdateDemandDraftDTO updateDemandDraftDTO) {
        Store store = storeEntityService.getEntityById(updateDemandDraftDTO.storeId());
        Integer year = updateDemandDraftDTO.year();
        Integer month = updateDemandDraftDTO.month();
        Integer day = updateDemandDraftDTO.day();
        int[] hourlyDemand = updateDemandDraftDTO.hourlyDemand();

        DemandDraft demandDraft = demandDraftRepository.findByStoreAndYearAndMonthAndDay(store, year, month, day)
                .orElseGet(() ->
                        new DemandDraftBuilder().createDemandDraft(
                                store,
                                year,
                                month,
                                day,
                                hourlyDemand
                        )
                );

        demandDraftMapper.updateDemandDraft(updateDemandDraftDTO,demandDraft);

        DemandDraft savedDemandDraft = demandDraftRepository.save(demandDraft);

        return demandDraftMapper.toResponseDemandDraftDTO(savedDemandDraft);
    }

    @Override
    public void deleteDemandDraft(Long id) {
        if (!demandDraftRepository.existsById(id)){
            throw new EntityNotFoundException("Cannot find demand draft by id " + id);
        }

        demandDraftRepository.deleteById(id);
    }

    @Override
    public List<ResponseDemandDraftDTO> findAll() {
        return demandDraftRepository.findAll().stream()
                .map(demandDraftMapper::toResponseDemandDraftDTO)
                .toList();
    }

    @Override
    public ResponseDemandDraftDTO findById(Long id) {
        DemandDraft demandDraft = demandDraftRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("Cannot find demand draft by id " + id));

        return demandDraftMapper.toResponseDemandDraftDTO(demandDraft);
    }

    @Override
    public boolean exists(Long id) {
        return demandDraftRepository.existsById(id);
    }

    @Override
    public boolean exists(StoreAccurateDayDemandDraftDTO dto) {
        Store store = storeEntityService.getEntityById(dto.storeId());

        return demandDraftRepository.existsByStoreAndYeahAndMonthAndDay(
                store,
                dto.year(),
                dto.month(),
                dto.day()
        );
    }

    @Override
    public DemandDraft saveEntity(DemandDraft demandDraft) {
        return demandDraftRepository.save(demandDraft);
    }

    @Override
    public DemandDraft getEntityById(Long id) {
        return demandDraftRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find demand draft by id " + id));
    }
}
