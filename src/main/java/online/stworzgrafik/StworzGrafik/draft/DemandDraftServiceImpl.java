package online.stworzgrafik.StworzGrafik.draft;

import de.focus_shift.jollyday.core.HolidayManager;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.draft.DTO.*;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.user.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class DemandDraftServiceImpl implements DemandDraftService, DemandDraftEntityService{
    private final DemandDraftRepository demandDraftRepository;
    private final StoreEntityService storeEntityService;
    private final DemandDraftMapper demandDraftMapper;
    private final UserContext userContext;
    private final UserAuthorizationService userAuthorizationService;
    private final HolidayManager holidayManager;
    private final CalendarCalculation calendarCalculation;
    private final EmployeeEntityService employeeEntityService;

    @Override
    public ResponseDemandDraftDTO createDemandDraft(Long storeId,CreateDemandDraftDTO dto) {
        Long validatedStoreId = userAuthorizationService.getUserAccessibleStoreId(storeId);

        Store store = storeEntityService.getEntityById(storeId);//change

        if (demandDraftRepository.existsByStoreIdAndDraftDate(storeId,dto.draftDate())){
            return demandDraftMapper.toResponseDemandDraftDTO(demandDraftRepository.findByStore_IdAndDraftDateBetween(storeId,dto.draftDate(),dto.draftDate())
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find demand draft on date " + dto.draftDate() + " for store with id " + storeId)));
        }

        int[] validatedDailyDemandDraft = holidayManager.isHoliday(dto.draftDate()) ? new int[24] : dto.hourlyDemand();

        DemandDraft demandDraft = new DemandDraftBuilder().createDemandDraft(
                store,
                dto.draftDate(),
                validatedDailyDemandDraft
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

        int[] validatedDailyDemandDraft = holidayManager.isHoliday(dto.draftDate()) ? new int[24] : dto.hourlyDemand();
        UpdateDemandDraftDTO validatedDto = new UpdateDemandDraftDTO(dto.draftDate(), validatedDailyDemandDraft);

        demandDraftMapper.updateDemandDraft(validatedDto,demandDraft);

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
    public Page<ResponseDemandDraftDTO> findAll(Pageable pageable) {
        return demandDraftRepository.findAll(pageable)
                .map(demandDraftMapper::toResponseDemandDraftDTO);
    }

    @Override
    public ResponseDemandDraftDTO findById(Long storeId, Long draftId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        DemandDraft demandDraft = demandDraftRepository.findById(draftId).
                orElseThrow(() -> new EntityNotFoundException("Cannot find demand draft by id " + draftId));

        if (!demandDraft.getStore().getId().equals(storeId)){
            throw new EntityNotFoundException("Demand draft does not belong to this store");
        }

        return demandDraftMapper.toResponseDemandDraftDTO(demandDraft);
    }

    @Override
    public Page<ResponseDemandDraftDTO> findFilteredDrafts(Long storeId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return findEntityFilteredDrafts(storeId,startDate,endDate,pageable)
                .map(demandDraftMapper::toResponseDemandDraftDTO);
    }

    @Override
    public boolean exists(Long draftId) {
        return demandDraftRepository.existsById(draftId);
    }

    @Override
    public boolean exists(StoreAccurateDayDemandDraftDTO dto) {
        return demandDraftRepository.existsByStoreIdAndDraftDate(
                dto.storeId(),
                dto.draftDate()
        );
    }

    @Override
    public BigDecimal getMonthlyDraftSum(Long storeId, Integer year, Integer month) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
        BigDecimal draftSum = BigDecimal.ZERO;
        YearMonth yearMonth = YearMonth.of(year,month);
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(year,month,day);

            Optional<DemandDraft> dateDraft = demandDraftRepository.findByStore_IdAndDraftDateBetween(storeId, date, date);
            if (dateDraft.isPresent()){
                BigDecimal dailyDraftSum = BigDecimal.valueOf(Arrays.stream(dateDraft.get().getHourlyDemand()).sum());

                draftSum = draftSum.add(dailyDraftSum);
            }
        }

        return draftSum;
    }

    @Override
    public MonthlyNormResponseDTO getMonthlyNorm(Long storeId, Integer year, Integer month) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        // 1. Standardowe godziny robocze (z uwzgl. świąt) — np. 160 dla maja 2026
        int standardWorkingHours = calendarCalculation.getMonthlyStandardWorkingHours(year, month);

        // 2. Aktywni pracownicy sklepu bez magazyniera
        List<Employee> nonWarehouseEmployees = employeeEntityService
                .findAllStoreActiveEmployees(storeId)
                .stream()
                .filter(e -> !e.isWarehouseman())
                .toList();

        // 3. Suma indywidualnych norm (uwzgl. wymiar etatu i ewentualne normy specjalne)
        BigDecimal totalEmployeeNorm = nonWarehouseEmployees.stream()
                .map(e -> calendarCalculation.getMonthlyNormForEmployee(year, month, e))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyNormResponseDTO(
                standardWorkingHours,
                totalEmployeeNorm,
                nonWarehouseEmployees.size()
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

    @Override
    public Page<DemandDraft> findEntityFilteredDrafts(Long storeId, @Nullable LocalDate startDate, @Nullable LocalDate endDate, Pageable pageable) {
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

        return demandDraftRepository.findByStore_IdAndDraftDateBetween(storeId, startDate, endDate,pageable);
    }

    @Override
    public List<DemandDraft> findAllByStoreIdAndDateBetween(Long storeId, LocalDate startDay, LocalDate endDay) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        return demandDraftRepository.findAllByStore_IdAndDraftDateBetween(storeId,startDay,endDay);
    }
}
