package online.stworzgrafik.StworzGrafik.schedule.hours;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.PeriodHoursCorrectionDTO;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.PeriodHoursCorrectionItemRequest;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.SavePeriodHoursCorrectionsRequest;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class PeriodHoursCorrectionServiceImpl implements PeriodHoursCorrectionService {

    private final PeriodHoursCorrectionRepository repository;
    private final EmployeeEntityService employeeEntityService;
    private final StoreEntityService storeEntityService;
    private final ScheduleDetailsService scheduleDetailsService;
    private final BillingPeriodConfigService billingPeriodConfigService;

    @Override
    @Transactional(readOnly = true)
    public List<PeriodHoursCorrectionDTO> getForStore(Long storeId, Integer year, Integer month) {
        List<Employee> employees = employeeEntityService.findAllStoreActiveEmployees(storeId);
        List<Integer> previousMonths = getPreviousMonthsInPeriod(year, month);

        return employees.stream().map(employee -> {
            BigDecimal calculated = sumFromScheduleDetails(storeId, employee.getId(), year, previousMonths);

            BigDecimal corrected = previousMonths.stream()
                    .map(m -> {
                        int y = (m < getStartMonthOfPeriod(year, month)) ? year + 1 : year;
                        return repository.findByStore_IdAndEmployee_IdAndYearAndMonth(
                                        storeId, employee.getId(), y, m)
                                .map(PeriodHoursCorrection::getCorrectedHours)
                                .orElse(null);
                    })
                    .filter(hours -> hours != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean hasAnyCorrection = previousMonths.stream().anyMatch(m -> {
                int y = (m < getStartMonthOfPeriod(year, month)) ? year + 1 : year;
                return repository.findByStore_IdAndEmployee_IdAndYearAndMonth(
                        storeId, employee.getId(), y, m).isPresent();
            });

            return new PeriodHoursCorrectionDTO(
                    employee.getId(),
                    employee.getFirstName() + " " + employee.getLastName(),
                    calculated,
                    hasAnyCorrection ? corrected : null
            );
        }).toList();
    }

    @Override
    @Transactional
    public void saveCorrections(Long storeId, Integer year, Integer month, SavePeriodHoursCorrectionsRequest request) {
        Store store = storeEntityService.getEntityById(storeId);
        List<Integer> previousMonths = getPreviousMonthsInPeriod(year, month);
        int startMonth = getStartMonthOfPeriod(year, month);

        for (PeriodHoursCorrectionItemRequest item : request.corrections()) {
            Employee employee = employeeEntityService.getEntityById(item.employeeId());

            Integer firstPreviousMonth = previousMonths.get(0);
            int recordYear = (firstPreviousMonth < startMonth) ? year + 1 : year;

            PeriodHoursCorrection correction = repository
                    .findByStore_IdAndEmployee_IdAndYearAndMonth(
                            storeId, item.employeeId(), recordYear, firstPreviousMonth)
                    .orElseGet(() -> PeriodHoursCorrection.builder()
                            .store(store)
                            .employee(employee)
                            .year(recordYear)
                            .month(firstPreviousMonth)
                            .build());

            correction.setCorrectedHours(item.correctedHours());
            repository.save(correction);
        }

        log.info("Saved period hours corrections for store {} year {} month {}",
                storeId, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getHoursWorkedSoFarInPeriod(Long storeId, Long employeeId, Integer year, Integer month) {
        List<Integer> previousMonths = getPreviousMonthsInPeriod(year, month);
        int startMonth = getStartMonthOfPeriod(year, month);

        Integer firstPreviousMonth = previousMonths.isEmpty() ? null : previousMonths.get(0);
        if (firstPreviousMonth != null) {
            int recordYear = (firstPreviousMonth < startMonth) ? year + 1 : year;
            Optional<PeriodHoursCorrection> correction = repository
                    .findByStore_IdAndEmployee_IdAndYearAndMonth(
                            storeId, employeeId, recordYear, firstPreviousMonth);
            if (correction.isPresent()) {
                return correction.get().getCorrectedHours();
            }
        }

        return sumFromScheduleDetails(storeId, employeeId, year, previousMonths);
    }

    private List<Integer> getPreviousMonthsInPeriod(Integer year, Integer month) {
        int startMonth = getStartMonthOfPeriod(year, month);
        List<Integer> allMonths = billingPeriodConfigService.getPeriodMonths(startMonth,year);
        return allMonths.subList(0, allMonths.size() - 1);
    }

    private int getStartMonthOfPeriod(Integer year, Integer month) {
        return billingPeriodConfigService.getPeriodStartMonth(month);
    }

    private BigDecimal sumFromScheduleDetails(Long storeId, Long employeeId,
                                              Integer year, List<Integer> months) {
        if (months.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = BigDecimal.ZERO;
        for (Integer m : months) {
            int scheduleYear = (m < billingPeriodConfigService.getPeriodStartMonth(m))
                    ? year + 1 : year;
            BigDecimal monthHours = scheduleDetailsService
                    .getEmployeeSumHoursByMonth(
                            storeId, employeeId, scheduleYear, m);
            total = total.add(monthHours != null ? monthHours : BigDecimal.ZERO);
        }
        return total;
    }
}
