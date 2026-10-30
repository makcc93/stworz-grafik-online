package online.stworzgrafik.StworzGrafik.calendar.holidays;

import de.focus_shift.jollyday.core.HolidayManager;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayCreateRequest;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayInitializationResponse;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayResponse;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayUpdateRequest;
import online.stworzgrafik.StworzGrafik.security.CurrentUserProvider;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.label.UserLabelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rok z wpisami w tabeli holiday → baza jest źródłem prawdy.
 * Rok bez wpisów → fallback do jollyday (identyczne zachowanie jak przed wprowadzeniem tabeli).
 *
 * Każda zmiana w roku bez wpisów najpierw zapisuje w bazie wszystkie święta domyślne tego roku,
 * żeby pojedynczy dodany dzień nie wyparł pozostałych świąt.
 * Usunięcie ostatniego wpisu roku przywraca fallback do jollyday (resetYear robi to jawnie).
 */
@Service
@RequiredArgsConstructor
class HolidayServiceImpl implements HolidayService {
    private static final int FIRST_YEAR = 2001;
    private static final int LAST_YEAR = 2099;
    private static final int MAX_NAME_LENGTH = 100;
    private static final String DEFAULT_NAME = "Święto";
    private static final Locale POLISH = Locale.forLanguageTag("pl");

    // isHoliday jest wołane w pętlach algorytmu - cache w pamięci, a TTL leczy ewentualne
    // rozjechanie między instancjami aplikacji (zmiana w jednej, cache w drugiej)
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final HolidayRepository holidayRepository;
    private final HolidayManager holidayManager;
    private final CurrentUserProvider currentUserProvider;
    private final UserLabelService userLabelService;

    private final Map<Integer, CachedYear> cache = new ConcurrentHashMap<>();

    private record CachedYear(Set<LocalDate> dates, Instant loadedAt) {
        boolean isFresh() {
            return loadedAt.plus(CACHE_TTL).isAfter(Instant.now());
        }
    }

    // ==================== ODCZYT ====================

    @Override
    public boolean isHoliday(LocalDate date) {
        return datesForYear(date.getYear()).contains(date);
    }

    @Override
    public List<LocalDate> getHolidayDates(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got " + month);
        }

        return datesForYear(year).stream()
                .filter(date -> date.getMonthValue() == month)
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> findAllByYear(int year) {
        validateYear(year);

        List<Holiday> rows = holidayRepository.findAllByDateBetweenOrderByDate(startOf(year), endOf(year));

        if (!rows.isEmpty()) {
            return rows.stream().map(this::toResponse).toList();
        }

        return defaultHolidays(year).entrySet().stream()
                .map(entry -> new HolidayResponse(entry.getKey(), entry.getValue(), false, false,
                        null, null, null, null))
                .toList();
    }

    // ==================== ZAPIS ====================

    @Override
    @Transactional
    public HolidayResponse create(HolidayCreateRequest dto) {
        int year = validateDate(dto.date());
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        materializeYear(year, currentUser, label);

        if (holidayRepository.findByDate(dto.date()).isPresent()) {
            throw new EntityExistsException("Holiday already exists on " + dto.date());
        }

        Holiday saved = holidayRepository.save(Holiday.builder()
                .date(dto.date())
                .name(dto.name().trim())
                .custom(true)
                .createdByUserId(currentUser.getId())
                .createdByLabel(label)
                .build());

        evictCacheAfterCommit();

        return toResponse(saved);
    }

    @Override
    @Transactional
    public HolidayResponse update(LocalDate date, HolidayUpdateRequest dto) {
        int year = validateDate(date);
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        materializeYear(year, currentUser, label);

        Holiday holiday = findByDateOrThrow(date);
        holiday.setName(dto.name().trim());
        holiday.setUpdatedByUserId(currentUser.getId());
        holiday.setUpdatedByLabel(label);

        Holiday saved = holidayRepository.save(holiday);

        evictCacheAfterCommit();

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(LocalDate date) {
        int year = validateDate(date);
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        materializeYear(year, currentUser, label);

        holidayRepository.delete(findByDateOrThrow(date));

        evictCacheAfterCommit();
    }

    @Override
    @Transactional
    public HolidayInitializationResponse initializeDefaults() {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        List<Holiday> toCreate = new ArrayList<>();
        int initializedYears = 0;
        int skippedYears = 0;

        for (int year = FIRST_YEAR; year <= LAST_YEAR; year++) {
            if (holidayRepository.existsByDateBetween(startOf(year), endOf(year))) {
                skippedYears++;
                continue;
            }

            toCreate.addAll(buildDefaultRows(year, currentUser, label));
            initializedYears++;
        }

        holidayRepository.saveAll(toCreate);

        evictCacheAfterCommit();

        return new HolidayInitializationResponse(initializedYears, skippedYears, toCreate.size());
    }

    @Override
    @Transactional
    public List<HolidayResponse> resetYear(int year) {
        validateYear(year);
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        holidayRepository.deleteAllByDateBetween(startOf(year), endOf(year));

        List<Holiday> saved = holidayRepository.saveAll(buildDefaultRows(year, currentUser, label));

        evictCacheAfterCommit();

        return saved.stream().map(this::toResponse).toList();
    }

    // ==================== POMOCNICZE ====================

    private Set<LocalDate> datesForYear(int year) {
        CachedYear cached = cache.get(year);

        if (cached != null && cached.isFresh()) {
            return cached.dates();
        }

        List<Holiday> rows = holidayRepository.findAllByDateBetweenOrderByDate(startOf(year), endOf(year));

        Set<LocalDate> dates = rows.isEmpty()
                ? Set.copyOf(defaultHolidays(year).keySet())
                : rows.stream().map(Holiday::getDate).collect(java.util.stream.Collectors.toUnmodifiableSet());

        cache.put(year, new CachedYear(dates, Instant.now()));

        return dates;
    }

    /**
     * Święta domyślne (data → nazwa po polsku) z jollyday. Uwzględniamy tylko dni, które jollyday
     * uznaje za święto w isHoliday() - dokładnie to, czego aplikacja używała do tej pory.
     */
    private Map<LocalDate, String> defaultHolidays(int year) {
        Map<LocalDate, String> result = new TreeMap<>();

        holidayManager.getHolidays(year).forEach(holiday -> {
            LocalDate date = holiday.getDate();

            if (date.getYear() != year || !holidayManager.isHoliday(date)) {
                return;
            }

            String name;
            try {
                name = holiday.getDescription(POLISH);
            } catch (RuntimeException e) {
                name = null;
            }

            if (name == null || name.isBlank()) {
                name = DEFAULT_NAME;
            }

            result.putIfAbsent(date, name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name);
        });

        return result;
    }

    private List<Holiday> buildDefaultRows(int year, AppUser user, String label) {
        return defaultHolidays(year).entrySet().stream()
                .map(entry -> Holiday.builder()
                        .date(entry.getKey())
                        .name(entry.getValue())
                        .custom(false)
                        .createdByUserId(user.getId())
                        .createdByLabel(label)
                        .build())
                .toList();
    }

    /** Jeśli rok nie ma jeszcze wpisów, zapisuje w bazie wszystkie jego święta domyślne. */
    private void materializeYear(int year, AppUser user, String label) {
        if (!holidayRepository.existsByDateBetween(startOf(year), endOf(year))) {
            holidayRepository.saveAll(buildDefaultRows(year, user, label));
        }
    }

    private Holiday findByDateOrThrow(LocalDate date) {
        return holidayRepository.findByDate(date)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find holiday on " + date));
    }

    private HolidayResponse toResponse(Holiday holiday) {
        return new HolidayResponse(
                holiday.getDate(),
                holiday.getName(),
                holiday.isCustom(),
                true,
                holiday.getCreatedAt(),
                holiday.getCreatedByLabel(),
                holiday.getUpdatedAt(),
                holiday.getUpdatedByLabel()
        );
    }

    /** Cache czyścimy dopiero po commicie - inaczej inny wątek mógłby w międzyczasie załadować stare dane. */
    private void evictCacheAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cache.clear();
                }
            });
        } else {
            cache.clear();
        }
    }

    private int validateDate(LocalDate date) {
        validateYear(date.getYear());
        return date.getYear();
    }

    private void validateYear(int year) {
        if (year < FIRST_YEAR || year > LAST_YEAR) {
            throw new IllegalArgumentException("Year must be between " + FIRST_YEAR + " and " + LAST_YEAR + ", got " + year);
        }
    }

    private static LocalDate startOf(int year) {
        return LocalDate.of(year, 1, 1);
    }

    private static LocalDate endOf(int year) {
        return LocalDate.of(year, 12, 31);
    }
}