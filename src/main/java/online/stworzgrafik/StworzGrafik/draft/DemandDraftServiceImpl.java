package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.StoreAccurateDayDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.controller.DraftSearchCriteria;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
class DemandDraftServiceImpl implements DemandDraftService, DemandDraftEntityService{
    private final DemandDraftRepository demandDraftRepository;
    private final StoreEntityService storeEntityService;
    private final DemandDraftMapper demandDraftMapper;
    private final UserContext userContext;
    private final UserAuthorizationService userAuthorizationService;

    @Override
    public ResponseDemandDraftDTO createDemandDraft(Long storeId,CreateDemandDraftDTO dto) {
        Long validatedStoreId = userAuthorizationService.getUserAccessibleStoreId(storeId);

        Store store = storeEntityService.getEntityById(validatedStoreId);

        if (demandDraftRepository.existsByStoreIdAndDate(storeId,dto.draftDate())){
            throw new EntityExistsException("Store id " + storeId + " demand draft on date " + dto.draftDate() + " already exists");
        }

        DemandDraft demandDraft = new DemandDraftBuilder().createDemandDraft(
                store,
                dto.draftDate(),
                dto.hourlyDemand()
        );

        DemandDraft savedDemandDraft = demandDraftRepository.save(demandDraft);

        return demandDraftMapper.toResponseDemandDraftDTO(savedDemandDraft);
    }

    @Override
    public ResponseDemandDraftDTO updateDemandDraft(Long storeId,Long draftId, UpdateDemandDraftDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId + ", cannot update draft with id " + draftId);
        }

        DemandDraft demandDraft = demandDraftRepository.findById(draftId).orElseThrow(() ->
                new EntityNotFoundException("Cannot find demand draft by id " + draftId));

        demandDraftMapper.updateDemandDraft(dto,demandDraft);

        DemandDraft savedDemandDraft = demandDraftRepository.save(demandDraft);

        return demandDraftMapper.toResponseDemandDraftDTO(savedDemandDraft);
    }

    @Override
    public void deleteDemandDraft(Long storeId, Long draftId) {
        if (!demandDraftRepository.existsById(draftId)){
            throw new EntityNotFoundException("Cannot find demand draft by id " + draftId);
        }

        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        demandDraftRepository.deleteById(draftId);
    }

    @Override
    public List<ResponseDemandDraftDTO> findAll() {
        return demandDraftRepository.findAll().stream()
                .map(demandDraftMapper::toResponseDemandDraftDTO)
                .toList();
    }

    @Override
    public ResponseDemandDraftDTO findById(Long storeId, Long draftId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        DemandDraft demandDraft = demandDraftRepository.findById(draftId).
                orElseThrow(() -> new EntityNotFoundException("Cannot find demand draft by id " + draftId));

        if (demandDraft.getStore().getId().equals(storeId)){
            throw new EntityNotFoundException("Demand draft does not belong to this store");
        }

        return demandDraftMapper.toResponseDemandDraftDTO(demandDraft);
    }

    @Override
    public List<ResponseDemandDraftDTO> findFilteredDrafts(Long storeId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null){
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }
        else if (startDate != null && endDate == null){
            endDate = startDate;
        }
        else if (startDate == null && endDate != null){
            throw new IllegalArgumentException("Must provide start day when providing end day");
        }

        List<DemandDraft> drafts = demandDraftRepository.findByStoreIdAndDateBetween(storeId, startDate, endDate);

        return drafts.stream()
                .map(demandDraftMapper::toResponseDemandDraftDTO)
                .toList();
    }

    @Override
    public boolean exists(Long draftId) {
        return demandDraftRepository.existsById(draftId);
    }

    @Override
    public boolean exists(StoreAccurateDayDemandDraftDTO dto) {
        return demandDraftRepository.existsByStoreIdAndDate(
                dto.storeId(),
                dto.draftDate()
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
