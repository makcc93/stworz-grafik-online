package online.stworzgrafik.StworzGrafik.calendar.workingNorm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.calendar.StandardWorkingHoursCalculator;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormInitializationResponse;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormRequest;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.DTO.WorkingNormResponse;
import online.stworzgrafik.StworzGrafik.security.CurrentUserProvider;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.label.UserLabelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
class WorkingNormServiceImpl implements WorkingNormService {
    private static final int FIRST_YEAR = 2001;
    private static final int LAST_YEAR = 2099;

    private final WorkingNormRepository workingNormRepository;
    private final StandardWorkingHoursCalculator standardWorkingHoursCalculator;
    private final CurrentUserProvider currentUserProvider;
    private final UserLabelService userLabelService;

    private final Set<Integer> warnedMissingMonths = ConcurrentHashMap.newKeySet();

    @Override
    @Transactional
    public WorkingNormResponse update(int year, int month, WorkingNormRequest dto) {
        validate(year, month);

        return toResponse(upsert(year, month, dto.hours()), year, month);
    }

    @Override
    @Transactional
    public WorkingNormResponse resetToDefault(int year, int month) {
        validate(year, month);

        int standardHours = standardWorkingHoursCalculator.getDefaultMonthlyHours(year, month);

        return toResponse(upsert(year, month, standardHours), year, month);
    }

    @Override
    @Transactional
    public WorkingNormInitializationResponse initializeDefaults() {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        Set<Integer> existing = workingNormRepository.findAll().stream()
                .map(norm -> key(norm.getYear(), norm.getMonth()))
                .collect(Collectors.toSet());

        List<WorkingNorm> toCreate = new ArrayList<>();
        int total = 0;

        for (int year = FIRST_YEAR; year <= LAST_YEAR; year++) {
            for (int month = 1; month <= 12; month++) {
                total++;

                if (existing.contains(key(year, month))) {
                    continue;
                }

                toCreate.add(WorkingNorm.builder()
                        .year(year)
                        .month(month)
                        .hours(standardWorkingHoursCalculator.getDefaultMonthlyHours(year, month))
                        .createdByUserId(currentUser.getId())
                        .createdByLabel(label)
                        .build());
            }
        }

        workingNormRepository.saveAll(toCreate);

        return new WorkingNormInitializationResponse(toCreate.size(), total - toCreate.size());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkingNormResponse findByYearMonth(int year, int month) {
        validate(year, month);

        WorkingNorm workingNorm = workingNormRepository.findByYearAndMonth(year, month).orElse(null);

        return toResponse(workingNorm, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkingNormResponse> findAllByYear(int year) {
        validate(year, 1);

        Map<Integer, WorkingNorm> byMonth = workingNormRepository.findAllByYear(year).stream()
                .collect(Collectors.toMap(WorkingNorm::getMonth, Function.identity()));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> toResponse(byMonth.get(month), year, month))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getHours(int year, int month) {
        return workingNormRepository.findByYearAndMonth(year, month)
                .map(WorkingNorm::getHours)
                .orElseGet(() -> {
                    if (warnedMissingMonths.add(key(year, month))) {
                        log.warn("Working norm for {}-{} is not set, using default (working days * 8h). " +
                                "Run POST /api/workingNorm/initialize to store defaults.", year, month);
                    }

                    return standardWorkingHoursCalculator.getDefaultMonthlyHours(year, month);
                });
    }

    private WorkingNorm upsert(int year, int month, int hours) {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        WorkingNorm workingNorm = workingNormRepository.findByYearAndMonth(year, month).orElse(null);

        if (workingNorm == null) {
            workingNorm = WorkingNorm.builder()
                    .year(year)
                    .month(month)
                    .hours(hours)
                    .createdByUserId(currentUser.getId())
                    .createdByLabel(label)
                    .build();
        } else {
            workingNorm.setHours(hours);
            workingNorm.setUpdatedByUserId(currentUser.getId());
            workingNorm.setUpdatedByLabel(label);
        }

        return workingNormRepository.save(workingNorm);
    }

    private WorkingNormResponse toResponse(WorkingNorm workingNorm, int year, int month) {
        int standardHours = standardWorkingHoursCalculator.getDefaultMonthlyHours(year, month);

        if (workingNorm == null) {
            return new WorkingNormResponse(year, month, standardHours, standardHours, false,
                    null, null, null, null, null, null);
        }

        return new WorkingNormResponse(
                workingNorm.getYear(),
                workingNorm.getMonth(),
                workingNorm.getHours(),
                standardHours,
                true,
                workingNorm.getCreatedAt(),
                workingNorm.getCreatedByUserId(),
                workingNorm.getCreatedByLabel(),
                workingNorm.getUpdatedAt(),
                workingNorm.getUpdatedByUserId(),
                workingNorm.getUpdatedByLabel()
        );
    }

    private void validate(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got " + month);
        }

        if (year < FIRST_YEAR || year > LAST_YEAR) {
            throw new IllegalArgumentException("Year must be between " + FIRST_YEAR + " and " + LAST_YEAR + ", got " + year);
        }
    }

    private static int key(int year, int month) {
        return year * 100 + month;
    }
}