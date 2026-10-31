package online.stworzgrafik.StworzGrafik.draft.template;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.holidays.HolidayService;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftService;
import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.template.DTO.*;
import online.stworzgrafik.StworzGrafik.security.CurrentUserProvider;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.label.UserLabelService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class DemandTemplateServiceImpl implements DemandTemplateService {
    private static final int FIRST_YEAR = 2001;
    private static final int LAST_YEAR = 2099;
    private static final int HOURS_IN_DAY = 24;

    private final DemandTemplateRepository demandTemplateRepository;
    private final DemandDraftService demandDraftService;
    private final DemandDraftEntityService demandDraftEntityService;
    private final HolidayService holidayService;
    private final StoreEntityService storeEntityService;
    private final UserAuthorizationService userAuthorizationService;
    private final CurrentUserProvider currentUserProvider;
    private final UserLabelService userLabelService;

    @Override
    @Transactional(readOnly = true)
    public List<DemandTemplateResponse> findByMonth(Long storeId, int year, int month) {
        verifyAccess(storeId);
        validate(year, month);

        return loadMonth(storeId, year, month);
    }

    @Override
    @Transactional
    public DemandTemplateSaveResponse save(Long storeId, int year, int month, DayOfWeek dayOfWeek, DemandTemplateRequest request) {
        verifyAccess(storeId);
        validate(year, month);
        validateDemand(request.hourlyDemand());
        storeEntityService.getEntityById(storeId); // 404 zamiast błędu FK, gdy sklep nie istnieje

        DemandTemplate template = upsert(storeId, year, month, dayOfWeek, request.hourlyDemand());

        int[] counts = new int[3];
        applyToDays(storeId, year, month, dayOfWeek, template.getHourlyDemand(), loadDraftsByDate(storeId, year, month), counts);

        return new DemandTemplateSaveResponse(
                toResponse(template, storeId, year, month, dayOfWeek),
                new DemandTemplateApplyResponse(counts[0], counts[1], counts[2])
        );
    }

    @Override
    @Transactional
    public DemandTemplateApplyResponse apply(Long storeId, int year, int month, DemandTemplateDaysRequest request) {
        verifyAccess(storeId);
        validate(year, month);

        Set<DayOfWeek> wanted = resolveDays(request == null ? null : request.dayOfWeeks());
        Map<LocalDate, DemandDraft> drafts = loadDraftsByDate(storeId, year, month);

        int[] counts = new int[3];
        for (DemandTemplate template : demandTemplateRepository.findAllByStoreIdAndYearAndMonth(storeId, year, month)) {
            if (!wanted.contains(template.getDayOfWeek())) {
                continue;
            }

            applyToDays(storeId, year, month, template.getDayOfWeek(), template.getHourlyDemand(), drafts, counts);
        }

        return new DemandTemplateApplyResponse(counts[0], counts[1], counts[2]);
    }

    @Override
    @Transactional
    public DemandTemplateCopyResponse copy(Long storeId, int year, int month, DemandTemplateCopyRequest request) {
        verifyAccess(storeId);
        validate(year, month);
        validate(request.targetYear(), request.targetMonth());

        if (request.targetYear() == year && request.targetMonth() == month) {
            throw new IllegalArgumentException("Source and target month must be different");
        }

        storeEntityService.getEntityById(storeId);

        Set<DayOfWeek> wanted = resolveDays(request.dayOfWeeks());
        List<DemandTemplate> source = demandTemplateRepository.findAllByStoreIdAndYearAndMonth(storeId, year, month).stream()
                .filter(template -> wanted.contains(template.getDayOfWeek()))
                .toList();

        for (DemandTemplate template : source) {
            upsert(storeId, request.targetYear(), request.targetMonth(), template.getDayOfWeek(), template.getHourlyDemand());
        }

        return new DemandTemplateCopyResponse(source.size(), loadMonth(storeId, request.targetYear(), request.targetMonth()));
    }

    private DemandTemplate upsert(Long storeId, int year, int month, DayOfWeek dayOfWeek, int[] hourlyDemand) {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        DemandTemplate template = demandTemplateRepository
                .findByStoreIdAndYearAndMonthAndDayOfWeek(storeId, year, month, dayOfWeek)
                .orElse(null);

        if (template == null) {
            template = DemandTemplate.builder()
                    .storeId(storeId)
                    .year(year)
                    .month(month)
                    .dayOfWeek(dayOfWeek)
                    .hourlyDemand(hourlyDemand.clone())
                    .createdByUserId(currentUser.getId())
                    .createdByLabel(label)
                    .build();
        } else {
            template.setHourlyDemand(hourlyDemand.clone());
            template.setUpdatedByUserId(currentUser.getId());
            template.setUpdatedByLabel(label);
        }

        return demandTemplateRepository.save(template);
    }

    private void applyToDays(Long storeId, int year, int month, DayOfWeek dayOfWeek, int[] hourlyDemand,
                             Map<LocalDate, DemandDraft> existingDrafts, int[] counts) {
        boolean hasDemand = Arrays.stream(hourlyDemand).anyMatch(value -> value > 0);

        for (LocalDate date : datesOf(year, month, dayOfWeek)) {
            DemandDraft existing = existingDrafts.get(date);

            if (existing != null) {
                demandDraftService.updateDemandDraft(storeId, existing.getId(), new UpdateDemandDraftDTO(date, hourlyDemand.clone()));
                counts[1]++;
            } else if (hasDemand && !holidayService.isHoliday(date)) {
                demandDraftService.createDemandDraft(storeId, new CreateDemandDraftDTO(date, hourlyDemand.clone()));
                counts[0]++;
            } else {
                counts[2]++;
            }
        }
    }

    private Map<LocalDate, DemandDraft> loadDraftsByDate(Long storeId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);

        return demandDraftEntityService
                .findAllByStoreIdAndDateBetween(storeId, yearMonth.atDay(1), yearMonth.atEndOfMonth()).stream()
                .collect(Collectors.toMap(DemandDraft::getDraftDate, Function.identity(), (first, second) -> first));
    }

    private List<DemandTemplateResponse> loadMonth(Long storeId, int year, int month) {
        Map<DayOfWeek, DemandTemplate> byDay = demandTemplateRepository
                .findAllByStoreIdAndYearAndMonth(storeId, year, month).stream()
                .collect(Collectors.toMap(DemandTemplate::getDayOfWeek, Function.identity(), (first, second) -> first));

        return Arrays.stream(DayOfWeek.values())
                .map(dayOfWeek -> toResponse(byDay.get(dayOfWeek), storeId, year, month, dayOfWeek))
                .toList();
    }

    private DemandTemplateResponse toResponse(DemandTemplate template, Long storeId, int year, int month, DayOfWeek dayOfWeek) {
        if (template == null) {
            return new DemandTemplateResponse(null, storeId, year, month, dayOfWeek, new int[HOURS_IN_DAY], false,
                    null, null, null, null, null, null);
        }

        return new DemandTemplateResponse(
                template.getId(),
                template.getStoreId(),
                template.getYear(),
                template.getMonth(),
                template.getDayOfWeek(),
                template.getHourlyDemand().clone(),
                true,
                template.getCreatedAt(),
                template.getCreatedByUserId(),
                template.getCreatedByLabel(),
                template.getUpdatedAt(),
                template.getUpdatedByUserId(),
                template.getUpdatedByLabel()
        );
    }

    private static List<LocalDate> datesOf(int year, int month, DayOfWeek dayOfWeek) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = YearMonth.of(year, month).atDay(1).with(TemporalAdjusters.nextOrSame(dayOfWeek));

        while (date.getMonthValue() == month) {
            dates.add(date);
            date = date.plusWeeks(1);
        }

        return dates;
    }

    private static Set<DayOfWeek> resolveDays(List<DayOfWeek> dayOfWeeks) {
        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            return EnumSet.allOf(DayOfWeek.class);
        }

        return EnumSet.copyOf(dayOfWeeks);
    }

    private void verifyAccess(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }

    private static void validate(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got " + month);
        }

        if (year < FIRST_YEAR || year > LAST_YEAR) {
            throw new IllegalArgumentException("Year must be between " + FIRST_YEAR + " and " + LAST_YEAR + ", got " + year);
        }
    }

    private static void validateDemand(int[] hourlyDemand) {
        if (hourlyDemand == null || hourlyDemand.length != HOURS_IN_DAY) {
            throw new IllegalArgumentException("Demand template must have exactly " + HOURS_IN_DAY + " elements");
        }

        if (Arrays.stream(hourlyDemand).anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException("Demand template values cannot be negative");
        }
    }
}