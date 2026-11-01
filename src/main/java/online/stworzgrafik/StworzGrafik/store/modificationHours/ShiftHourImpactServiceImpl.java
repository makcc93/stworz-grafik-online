package online.stworzgrafik.StworzGrafik.store.modificationHours;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourImpactResponse;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourRuleImpact;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ShiftHourImpactServiceImpl implements ShiftHourImpactService {
    private static final int FIRST_YEAR = 2001;
    private static final int LAST_YEAR = 2099;
    private static final int SECONDS_IN_DAY = 24 * 60 * 60;

    private final ShiftHourModificationConfigRepository repository;
    private final DemandDraftEntityService demandDraftEntityService;
    private final EmployeeEntityService employeeEntityService;
    private final UserAuthorizationService userAuthorizationService;

    @Override
    @Transactional(readOnly = true)
    public ShiftHourImpactResponse calculate(Long storeId, int year, int month) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        validate(year, month);

        List<Employee> employees = employeeEntityService.findAllStoreActiveEmployees(storeId).stream()
                .filter(employee -> !employee.isWarehouseman())
                .toList();
        int totalEmployees = employees.size();

        ShiftHourModificationConfig config = repository.findByStoreId(storeId).orElse(null);
        if (config == null || config.getRules().isEmpty() || totalEmployees == 0) {
            return empty(totalEmployees);
        }

        Set<Long> excludedIds = config.getExcludedEmployees().stream()
                .map(Employee::getId)
                .collect(Collectors.toSet());
        int eligibleEmployees = (int) employees.stream()
                .filter(employee -> !Boolean.TRUE.equals(employee.getIsSpecial()))
                .filter(employee -> !excludedIds.contains(employee.getId()))
                .count();

        int[] startsByHour = new int[24];
        int[] endsByHour = new int[24];
        YearMonth yearMonth = YearMonth.of(year, month);
        for (DemandDraft draft : demandDraftEntityService.findAllByStoreIdAndDateBetween(storeId, yearMonth.atDay(1), yearMonth.atEndOfMonth())) {
            countShiftEdges(draft.getHourlyDemand(), startsByHour, endsByHour);
        }

        BigDecimal share = BigDecimal.valueOf(eligibleEmployees)
                .divide(BigDecimal.valueOf(totalEmployees), 6, RoundingMode.HALF_UP);

        List<ShiftHourRuleImpact> rules = new ArrayList<>();
        BigDecimal totalExtraHours = BigDecimal.ZERO;

        List<ShiftHourRule> sortedRules = config.getRules().stream()
                .sorted(Comparator.comparing(ShiftHourRule::getOriginalHour).thenComparing(ShiftHourRule::getScope))
                .toList();

        for (ShiftHourRule rule : sortedRules) {
            LocalTime original = rule.getOriginalHour();
            LocalTime modified = rule.getModifiedHour();
            ShiftHourScope scope = rule.getScope();

            boolean wholeHour = original.getMinute() == 0 && original.getSecond() == 0;
            int starting = wholeHour && scope.appliesToStart() ? startsByHour[original.getHour()] : 0;
            int ending = wholeHour && scope.appliesToEnd() ? endsByHour[original.getHour()] : 0;

            long startMinutes = signedMinutesBetween(modified, original);
            long endMinutes = signedMinutesBetween(original, modified);
            long extraMinutes = starting * startMinutes + ending * endMinutes;

            BigDecimal extraHours = BigDecimal.valueOf(extraMinutes)
                    .multiply(share)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            rules.add(new ShiftHourRuleImpact(original, modified, scope, starting, ending, extraHours));
            totalExtraHours = totalExtraHours.add(extraHours);
        }

        BigDecimal perEmployee = totalExtraHours.divide(BigDecimal.valueOf(totalEmployees), 2, RoundingMode.HALF_UP);

        return new ShiftHourImpactResponse(totalExtraHours, perEmployee, eligibleEmployees, totalEmployees, rules);
    }

    private static void countShiftEdges(int[] demand, int[] startsByHour, int[] endsByHour) {
        if (demand == null || demand.length != 24) {
            return;
        }

        for (int hour = 0; hour < 24; hour++) {
            int previous = hour == 0 ? 0 : demand[hour - 1];
            if (demand[hour] > previous) {
                startsByHour[hour] += demand[hour] - previous;
            }

            int next = hour == 23 ? 0 : demand[hour + 1];
            if (demand[hour] > next) {
                endsByHour[(hour + 1) % 24] += demand[hour] - next;
            }
        }
    }

    private static long signedMinutesBetween(LocalTime from, LocalTime to) {
        int diff = to.toSecondOfDay() - from.toSecondOfDay();
        if (diff > SECONDS_IN_DAY / 2) {
            diff -= SECONDS_IN_DAY;
        } else if (diff <= -SECONDS_IN_DAY / 2) {
            diff += SECONDS_IN_DAY;
        }

        return diff / 60;
    }

    private static ShiftHourImpactResponse empty(int totalEmployees) {
        return new ShiftHourImpactResponse(BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2), 0, totalEmployees, List.of());
    }

    private static void validate(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got " + month);
        }

        if (year < FIRST_YEAR || year > LAST_YEAR) {
            throw new IllegalArgumentException("Year must be between " + FIRST_YEAR + " and " + LAST_YEAR + ", got " + year);
        }
    }
}