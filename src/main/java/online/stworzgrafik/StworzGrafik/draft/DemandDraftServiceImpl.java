package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.StoreAccurateDayDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.temporaryUser.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class DemandDraftServiceImpl implements DemandDraftService, DemandDraftEntityService{
    private final DemandDraftRepository demandDraftRepository;
    private final StoreEntityService storeEntityService;
    private final DemandDraftMapper demandDraftMapper;

    @Override
    public ResponseDemandDraftDTO createDemandDraft(Long storeId, CreateDemandDraftDTO dto) {
        Store store = storeEntityService.getEntityById(storeId);
        Integer year = dto.year();
        Integer month = dto.month();
        Integer day = dto.day();
        int[] hourlyDemand = dto.hourlyDemand();

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
    public ResponseDemandDraftDTO updateDemandDraft(Long storeId, UpdateDemandDraftDTO dto) {
        CurrentUser
                //potrzebuje chwilowo zrobic fake usera zeby go uzywac wewnatrz serwisu
        //potem pewnie strategy lub cos takiego zeby w zaleznosci od usera dobrac to czy storeId bedzie ten podany w endpoincie czy ten pobrany
        //z sesji logowania usera

        //potem zmiana currentUser na user-service jako mikroserwis

        Store store = storeEntityService.getEntityById(storeId);
        Integer year = dto.year();
        Integer month = dto.month();
        Integer day = dto.day();
        int[] hourlyDemand = dto.hourlyDemand();

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

        demandDraftMapper.updateDemandDraft(dto,demandDraft);

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
