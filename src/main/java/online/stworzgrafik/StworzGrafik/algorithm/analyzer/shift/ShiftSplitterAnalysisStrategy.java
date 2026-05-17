package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.DividedShiftDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.ShiftSwapCandidate;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftSplitterAnalysisStrategy implements ScheduleAnalysisStrategy {
    private final CalendarCalculation calendarCalculation;
    private final HolidayManager holidayManager;

    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.SHIFT_SPLITTER;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        LinkedHashMap<Employee,Integer> workingDaysCount = new LinkedHashMap<>();

        for (Employee employee : employees) {
            Integer value = context.getWorkingDaysCount().getOrDefault(employee, 0);

            workingDaysCount.put(employee,value);
        }

        LinkedHashMap<Employee, Integer> filteredWorkingDaysCountSortedAsc = workingDaysCount.entrySet().stream()
                .filter(entry -> !entry.getKey().isWarehouseman())
                .filter(entry -> !entry.getKey().isCashier())
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        if (filteredWorkingDaysCountSortedAsc.isEmpty()){
            log.info("Pusta mapa z pracowniczą liczbą dni pracy");
        }

        Integer lowestValue = filteredWorkingDaysCountSortedAsc.entrySet().stream().findFirst().get().getValue();

       return new ShiftSplitterAnalysisResult(monthlyMaxWorkingDays,lowestValue);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((ShiftSplitterAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        log.info("PRÓBA DZIELENIA ZMIAN");
        int monthlyMaxWorkingDays = ((ShiftSplitterAnalysisResult) result).monthlyMaxWorkingDays();
        while (context.getWorkingDaysCount().entrySet().stream()
                .anyMatch(e -> e.getValue() <= monthlyMaxWorkingDays - 2)) {
            boolean resolved = splitShiftsForManagers(context);
                    resolved |= splitShiftsForCredits(context);
                    resolved |= splitShiftsForCheckouts(context);
                    resolved |= splitShiftsForOthers(context);

            if (!resolved) break;
        }
    }

    private boolean splitShiftsForManagers(ScheduleGeneratorContext context){
            boolean anySwapDone = false;
            int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());
            int wantedMaxWorkingDays = monthlyMaxWorkingDays - 1;

            List<Employee> managers = context.getStoreActiveEmployees().stream()
                    .filter(Employee::isCanOpenCloseStore)
                    .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                    .toList();

            Map<Employee, Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();
            YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);
                for (Employee employee : managers) {
                    if (!context.employeeIsWorking(employee, date)) continue;
                    if (context.employeeIsOnVacation(employee, date)) continue;
                    if (context.employeeIsOnDelegation(employee, date)) continue;
                    if (context.employeeHasProposalShift(employee, date)) continue;
                    if (context.employeeHasProposalDaysOff(employee, date)) continue;
                    if (context.isEmployeeWorkingInWarehouse(employee, date)) continue;
                    if (context.isEmployeeWorkingOnCredit(employee, date)) continue;
                    if (context.isEmployeeWorkingOnCheckout(employee, date)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(employee, date)) continue;

                    Shift shift = context.getFinalSchedule()
                            .getOrDefault(date, new HashMap<>())
                            .getOrDefault(employee, context.findShiftByArray(new int[24]));

                    daysWithShiftsToSplit
                            .computeIfAbsent(employee, k -> new HashMap<>())
                            .put(date, shift);
                }
            }

            LinkedHashMap<Employee, Map<LocalDate, Shift>> sortedData = daysWithShiftsToSplit.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0)))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().entrySet().stream()
                                    .sorted((a, b) -> context.getShiftLength(b.getValue()).compareTo(context.getShiftLength(a.getValue())))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new)),
                            (v1, v2) -> v1,
                            LinkedHashMap::new
                    ));

            for (Map.Entry<Employee, Map<LocalDate, Shift>> entry : sortedData.entrySet()) {
                Employee employee = entry.getKey();
                Map<LocalDate, Shift> employeeDateShift = entry.getValue();

                List<ShiftSwapCandidate> swapCandidates = employeeDateShift.entrySet().stream()
                        .flatMap(insideEntry -> {
                            LocalDate insideDate = insideEntry.getKey();
                            Shift insideShift = insideEntry.getValue();

                            return managers.stream()
                                    .filter(empl -> !empl.equals(employee))
                                    .filter(sortedData::containsKey)
                                    .filter(empl -> !context.employeeIsWorking(empl, insideDate))
                                    .flatMap(empl -> sortedData.get(empl).entrySet().stream()
                                            .filter(otherEmplEntry -> {
                                                LocalDate otherDate = otherEmplEntry.getKey();
                                                Shift otherShift = otherEmplEntry.getValue();
                                                return insideShift.equals(otherShift) && !context.employeeIsWorking(employee, otherDate)
                                                        && context.getShiftLength(insideShift).compareTo(BigDecimal.valueOf(9)) > 0;
                                            })
                                            .map(otherEmplEntry -> new ShiftSwapCandidate(employee, empl, insideDate, otherEmplEntry.getKey(), insideShift)));
                        }).toList();

                for (ShiftSwapCandidate candidate : swapCandidates) {
                    Employee originalEmployee = candidate.originalEmployee();
                    Employee otherEmployeeForSwap = candidate.employeeForSwapShift();
                    LocalDate originalEmployeeDate = candidate.originalDateForSwap();
                    LocalDate otherEmployeeDateForSwap = candidate.otherEmployeeDateForSwap();

                    if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= wantedMaxWorkingDays) break;
                    if (context.getWorkingDaysCount().getOrDefault(otherEmployeeForSwap, 0) >= wantedMaxWorkingDays)
                        continue;

                    if (context.employeeIsWorking(otherEmployeeForSwap, originalEmployeeDate)) continue;
                    if (context.employeeIsWorking(originalEmployee, otherEmployeeDateForSwap)) continue;

                    if (context.employeeIsOnVacation(originalEmployee,originalEmployeeDate)) continue;
                    if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;
                    if (context.employeeIsOnVacation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                    if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;

                    if (context.employeeIsOnDelegation(originalEmployee,originalEmployeeDate)) continue;
                    if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;
                    if (context.employeeIsOnDelegation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                    if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingInWarehouse(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingInWarehouse(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingOnCredit(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingOnCredit(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingOnCheckout(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingOnCheckout(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeOnRestRequirementDayOff(originalEmployee, otherEmployeeDateForSwap)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(otherEmployeeForSwap, originalEmployeeDate)) continue;

                    Shift currentShiftOnDate = context.getFinalSchedule()
                            .getOrDefault(originalEmployeeDate, new HashMap<>())
                            .get(originalEmployee);

                    if (currentShiftOnDate == null) continue;
                    if (context.getShiftLength(currentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                    Shift otherCurrentShiftOnDate = context.getFinalSchedule()
                            .getOrDefault(otherEmployeeDateForSwap, new HashMap<>())
                            .get(otherEmployeeForSwap);

                    if (otherCurrentShiftOnDate == null) continue;
                    if (context.getShiftLength(otherCurrentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                    if (isWeekendOrHoliday(candidate.originalDateForSwap()) || isWeekendOrHoliday(candidate.otherEmployeeDateForSwap())) continue;

                    DividedShiftDTO dividedShiftDTO = divideShift(currentShiftOnDate, context);

                    Shift shiftForOriginal = originalEmployee.isCashier() ? dividedShiftDTO.afternoonShift() : dividedShiftDTO.morningShift();
                    Shift shiftForOther = originalEmployee.isCashier() ? dividedShiftDTO.morningShift() : dividedShiftDTO.afternoonShift();

                    context.updateShiftOnSchedule(candidate.originalDateForSwap(), originalEmployee, shiftForOriginal);
                    context.registerShiftOnSchedule(candidate.originalDateForSwap(), otherEmployeeForSwap, shiftForOther, candidate.originalDateForSwap().getDayOfWeek());
                    context.assignEmployeeToOpenClose(candidate.originalDateForSwap(), otherEmployeeForSwap, shiftForOther);

                    context.updateShiftOnSchedule(candidate.otherEmployeeDateForSwap(), otherEmployeeForSwap, shiftForOther);
                    context.registerShiftOnSchedule(candidate.otherEmployeeDateForSwap(), originalEmployee, shiftForOriginal, candidate.otherEmployeeDateForSwap().getDayOfWeek());
                    context.assignEmployeeToOpenClose(candidate.otherEmployeeDateForSwap(), originalEmployee, shiftForOriginal);

                    anySwapDone = true;
                    break;
                }
            }
            return anySwapDone;
    }


    private boolean splitShiftsForCredits(ScheduleGeneratorContext context) {
        boolean anySwapDone = false;
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());
        int wantedMaxWorkingDays = monthlyMaxWorkingDays - 1;

        List<Employee> creditEmployees = context.getStoreActiveEmployees().stream()
                .filter(Employee::isCanOperateCredit)
                .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                .toList();

        Map<Employee, Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();
        YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);
            for (Employee employee : creditEmployees) {
                if (!context.employeeIsWorking(employee, date)) continue;
                if (!context.isEmployeeWorkingOnCredit(employee, date)) continue;
                if (context.employeeIsOnVacation(employee, date)) continue;
                if (context.employeeIsOnDelegation(employee, date)) continue;
                if (context.employeeHasProposalShift(employee, date)) continue;
                if (context.employeeHasProposalDaysOff(employee, date)) continue;
                if (context.isEmployeeWorkingInWarehouse(employee, date)) continue;
                if (context.isOpeningOrClosingStore(employee, date)) continue;
                if (context.isEmployeeWorkingOnCheckout(employee, date)) continue;
                if (context.isEmployeeOnRestRequirementDayOff(employee, date)) continue;

                Shift shift = context.getFinalSchedule()
                        .getOrDefault(date, new HashMap<>())
                        .getOrDefault(employee, context.findShiftByArray(new int[24]));

                daysWithShiftsToSplit
                        .computeIfAbsent(employee, k -> new HashMap<>())
                        .put(date, shift);
            }
            LinkedHashMap<Employee, Map<LocalDate, Shift>> sortedData = daysWithShiftsToSplit.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0)))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().entrySet().stream()
                                    .sorted((a, b) -> context.getShiftLength(b.getValue()).compareTo(context.getShiftLength(a.getValue())))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new)),
                            (v1, v2) -> v1,
                            LinkedHashMap::new
                    ));

            for (Map.Entry<Employee, Map<LocalDate, Shift>> entry : sortedData.entrySet()) {
                Employee employee = entry.getKey();
                Map<LocalDate, Shift> employeeDateShift = entry.getValue();

                List<ShiftSwapCandidate> swapCandidates = employeeDateShift.entrySet().stream()
                        .flatMap(insideEntry -> {
                            LocalDate insideDate = insideEntry.getKey();
                            Shift insideShift = insideEntry.getValue();

                            return creditEmployees.stream()
                                    .filter(empl -> !empl.equals(employee))
                                    .filter(sortedData::containsKey)
                                    .filter(empl -> !context.employeeIsWorking(empl, insideDate))
                                    .flatMap(empl -> sortedData.get(empl).entrySet().stream()
                                            .filter(otherEmplEntry -> {
                                                LocalDate otherDate = otherEmplEntry.getKey();
                                                Shift otherShift = otherEmplEntry.getValue();
                                                return insideShift.equals(otherShift) && !context.employeeIsWorking(employee, otherDate)
                                                        && context.getShiftLength(insideShift).compareTo(BigDecimal.valueOf(9)) > 0;
                                            })
                                            .map(otherEmplEntry -> new ShiftSwapCandidate(employee, empl, insideDate, otherEmplEntry.getKey(), insideShift)));
                        }).toList();

                for (ShiftSwapCandidate candidate : swapCandidates) {
                    Employee originalEmployee = candidate.originalEmployee();
                    Employee otherEmployeeForSwap = candidate.employeeForSwapShift();
                    LocalDate originalEmployeeDate = candidate.originalDateForSwap();
                    LocalDate otherEmployeeDateForSwap = candidate.otherEmployeeDateForSwap();

                    if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= wantedMaxWorkingDays) break;
                    if (context.getWorkingDaysCount().getOrDefault(otherEmployeeForSwap, 0) >= wantedMaxWorkingDays) continue;

                    if (context.employeeIsWorking(otherEmployeeForSwap, originalEmployeeDate)) continue;
                    if (context.employeeIsWorking(originalEmployee, otherEmployeeDateForSwap)) continue;

                    if (context.employeeIsOnVacation(originalEmployee,originalEmployeeDate)) continue;
                    if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;
                    if (context.employeeIsOnVacation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                    if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;

                    if (context.employeeIsOnDelegation(originalEmployee,originalEmployeeDate)) continue;
                    if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;
                    if (context.employeeIsOnDelegation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                    if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingInWarehouse(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingInWarehouse(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isOpeningOrClosingStore(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isOpeningOrClosingStore(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingOnCheckout(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingOnCheckout(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeOnRestRequirementDayOff(originalEmployee, otherEmployeeDateForSwap)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(otherEmployeeForSwap, originalEmployeeDate)) continue;

                    Shift currentShiftOnDate = context.getFinalSchedule()
                            .getOrDefault(originalEmployeeDate, new HashMap<>())
                            .get(originalEmployee);

                    if (currentShiftOnDate == null) continue;
                    if (context.getShiftLength(currentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                    Shift otherCurrentShiftOnDate = context.getFinalSchedule()
                            .getOrDefault(otherEmployeeDateForSwap, new HashMap<>())
                            .get(otherEmployeeForSwap);

                    if (otherCurrentShiftOnDate == null) continue;
                    if (context.getShiftLength(otherCurrentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                    if (isWeekendOrHoliday(candidate.originalDateForSwap()) || isWeekendOrHoliday(candidate.otherEmployeeDateForSwap())) continue;

                    DividedShiftDTO dividedShiftDTO = divideShift(currentShiftOnDate, context);

                    Shift shiftForOriginal = originalEmployee.isCashier() ? dividedShiftDTO.afternoonShift() : dividedShiftDTO.morningShift();
                    Shift shiftForOther    = originalEmployee.isCashier() ? dividedShiftDTO.morningShift()   : dividedShiftDTO.afternoonShift();

                    context.updateShiftOnSchedule(candidate.originalDateForSwap(), originalEmployee, shiftForOriginal);
                    context.registerShiftOnSchedule(candidate.originalDateForSwap(), otherEmployeeForSwap, shiftForOther, candidate.originalDateForSwap().getDayOfWeek());
                    context.assignEmployeeToCredit(candidate.originalDateForSwap(),otherEmployeeForSwap,shiftForOther);

                    context.updateShiftOnSchedule(candidate.otherEmployeeDateForSwap(), otherEmployeeForSwap, shiftForOther);
                    context.registerShiftOnSchedule(candidate.otherEmployeeDateForSwap(), originalEmployee, shiftForOriginal, candidate.otherEmployeeDateForSwap().getDayOfWeek());
                    context.assignEmployeeToCredit(candidate.otherEmployeeDateForSwap(),originalEmployee,shiftForOriginal);

                    anySwapDone = true;
                    break;
                }
                if (anySwapDone) break;
            }
        }
        return anySwapDone;
    }


    private boolean splitShiftsForCheckouts(ScheduleGeneratorContext context){
                boolean anySwapDone = false;
                int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());
                int wantedMaxWorkingDays = monthlyMaxWorkingDays - 1;

                List<Employee> checkoutEmployees = context.getStoreActiveEmployees().stream()
                        .filter(Employee::isCanOperateCheckout)
                        .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                        .toList();

                Map<Employee, Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();
                YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);
                    for (Employee employee : checkoutEmployees) {
                        if (!context.employeeIsWorking(employee, date)) continue;
                        if (!context.isEmployeeWorkingOnCheckout(employee, date)) continue;
                        if (context.employeeIsOnVacation(employee, date)) continue;
                        if (context.employeeIsOnDelegation(employee, date)) continue;
                        if (context.employeeHasProposalShift(employee, date)) continue;
                        if (context.employeeHasProposalDaysOff(employee, date)) continue;
                        if (context.isEmployeeWorkingInWarehouse(employee, date)) continue;
                        if (context.isOpeningOrClosingStore(employee, date)) continue;
                        if (context.isEmployeeWorkingOnCredit(employee, date)) continue;
                        if (context.isEmployeeOnRestRequirementDayOff(employee,date)) continue;

                        Shift shift = context.getFinalSchedule()
                                .getOrDefault(date, new HashMap<>())
                                .getOrDefault(employee, context.findShiftByArray(new int[24]));

                        daysWithShiftsToSplit
                                .computeIfAbsent(employee, k -> new HashMap<>())
                                .put(date, shift);
                    }
                    LinkedHashMap<Employee, Map<LocalDate, Shift>> sortedData = daysWithShiftsToSplit.entrySet().stream()
                            .sorted(Comparator.comparingInt(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0)))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().entrySet().stream()
                                            .sorted((a, b) -> context.getShiftLength(b.getValue()).compareTo(context.getShiftLength(a.getValue())))
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new)),
                                    (v1, v2) -> v1,
                                    LinkedHashMap::new
                            ));

                    for (Map.Entry<Employee, Map<LocalDate, Shift>> entry : sortedData.entrySet()) {
                        Employee employee = entry.getKey();
                        Map<LocalDate, Shift> employeeDateShift = entry.getValue();

                        List<ShiftSwapCandidate> swapCandidates = employeeDateShift.entrySet().stream()
                                .flatMap(insideEntry -> {
                                    LocalDate insideDate = insideEntry.getKey();
                                    Shift insideShift = insideEntry.getValue();

                                    return checkoutEmployees.stream()
                                            .filter(empl -> !empl.equals(employee))
                                            .filter(sortedData::containsKey)
                                            .filter(empl -> !context.employeeIsWorking(empl, insideDate))
                                            .flatMap(empl -> sortedData.get(empl).entrySet().stream()
                                                    .filter(otherEmplEntry -> {
                                                        LocalDate otherDate = otherEmplEntry.getKey();
                                                        Shift otherShift = otherEmplEntry.getValue();
                                                        return insideShift.equals(otherShift) && !context.employeeIsWorking(employee, otherDate)
                                                                && context.getShiftLength(insideShift).compareTo(BigDecimal.valueOf(9)) > 0;
                                                    })
                                                    .map(otherEmplEntry -> new ShiftSwapCandidate(employee, empl, insideDate, otherEmplEntry.getKey(), insideShift)));
                                }).toList();

                        for (ShiftSwapCandidate candidate : swapCandidates) {
                            Employee originalEmployee = candidate.originalEmployee();
                            Employee otherEmployeeForSwap = candidate.employeeForSwapShift();
                            LocalDate originalEmployeeDate = candidate.originalDateForSwap();
                            LocalDate otherEmployeeDateForSwap = candidate.otherEmployeeDateForSwap();

                            if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= wantedMaxWorkingDays) break;
                            if (context.getWorkingDaysCount().getOrDefault(otherEmployeeForSwap, 0) >= wantedMaxWorkingDays) continue;

                            if (context.employeeIsWorking(otherEmployeeForSwap, originalEmployeeDate)) continue;
                            if (context.employeeIsWorking(originalEmployee, otherEmployeeDateForSwap)) continue;

                            if (context.employeeIsOnVacation(originalEmployee,originalEmployeeDate)) continue;
                            if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;
                            if (context.employeeIsOnVacation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                            if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;

                            if (context.employeeIsOnDelegation(originalEmployee,originalEmployeeDate)) continue;
                            if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;
                            if (context.employeeIsOnDelegation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                            if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;

                            if (context.isEmployeeWorkingInWarehouse(originalEmployee, originalEmployeeDate)) continue;
                            if (context.isEmployeeWorkingInWarehouse(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                            if (context.isEmployeeWorkingOnCredit(originalEmployee, originalEmployeeDate)) continue;
                            if (context.isEmployeeWorkingOnCredit(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                            if (context.isOpeningOrClosingStore(originalEmployee, originalEmployeeDate)) continue;
                            if (context.isOpeningOrClosingStore(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                            if (context.isEmployeeOnRestRequirementDayOff(originalEmployee, otherEmployeeDateForSwap)) continue;
                            if (context.isEmployeeOnRestRequirementDayOff(otherEmployeeForSwap, originalEmployeeDate)) continue;

                            Shift currentShiftOnDate = context.getFinalSchedule()
                                    .getOrDefault(originalEmployeeDate, new HashMap<>())
                                    .get(originalEmployee);

                            if (currentShiftOnDate == null) continue;
                            if (context.getShiftLength(currentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                            Shift otherCurrentShiftOnDate = context.getFinalSchedule()
                                    .getOrDefault(otherEmployeeDateForSwap, new HashMap<>())
                                    .get(otherEmployeeForSwap);

                            if (otherCurrentShiftOnDate == null) continue;
                            if (context.getShiftLength(otherCurrentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                            if (isWeekendOrHoliday(candidate.originalDateForSwap()) || isWeekendOrHoliday(candidate.otherEmployeeDateForSwap())) continue;

                            DividedShiftDTO dividedShiftDTO = divideShift(currentShiftOnDate, context);

                            Shift shiftForOriginal = originalEmployee.isCashier() ? dividedShiftDTO.afternoonShift() : dividedShiftDTO.morningShift();
                            Shift shiftForOther    = originalEmployee.isCashier() ? dividedShiftDTO.morningShift()   : dividedShiftDTO.afternoonShift();

                            context.updateShiftOnSchedule(candidate.originalDateForSwap(), originalEmployee, shiftForOriginal);
                            context.registerShiftOnSchedule(candidate.originalDateForSwap(), otherEmployeeForSwap, shiftForOther, candidate.originalDateForSwap().getDayOfWeek());
                            context.assignEmployeeToCheckout(candidate.originalDateForSwap(),otherEmployeeForSwap,shiftForOther);

                            context.updateShiftOnSchedule(candidate.otherEmployeeDateForSwap(), otherEmployeeForSwap, shiftForOther);
                            context.registerShiftOnSchedule(candidate.otherEmployeeDateForSwap(), originalEmployee, shiftForOriginal, candidate.otherEmployeeDateForSwap().getDayOfWeek());
                            context.assignEmployeeToCheckout(candidate.otherEmployeeDateForSwap(),originalEmployee,shiftForOriginal);

                            anySwapDone = true;
                            break;
                        }
                        if (anySwapDone) break;
                    }
                }
        return anySwapDone;
    }


    private boolean splitShiftsForOthers(ScheduleGeneratorContext context){
        boolean anySwapDone = false;
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());
        int wantedMaxWorkingDays = monthlyMaxWorkingDays - 1;

        List<Employee> others = context.getStoreActiveEmployees().stream()
                .filter(empl -> !empl.isCanOperateCheckout())
                .filter(empl -> !empl.isCanOperateCredit())
                .filter(empl -> !empl.isCanOpenCloseStore())
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !empl.isCashier())
                .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                .toList();

        Map<Employee, Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();
        YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);
            for (Employee employee : others) {
                if (!context.employeeIsWorking(employee, date)) continue;
                if (context.isEmployeeWorkingOnCheckout(employee, date)) continue;
                if (context.employeeIsOnVacation(employee, date)) continue;
                if (context.employeeIsOnDelegation(employee, date)) continue;
                if (context.employeeHasProposalShift(employee, date)) continue;
                if (context.employeeHasProposalDaysOff(employee, date)) continue;
                if (context.isEmployeeWorkingInWarehouse(employee, date)) continue;
                if (context.isOpeningOrClosingStore(employee, date)) continue;
                if (context.isEmployeeWorkingOnCredit(employee, date)) continue;
                if (context.isEmployeeOnRestRequirementDayOff(employee,date)) continue;

                Shift shift = context.getFinalSchedule()
                        .getOrDefault(date, new HashMap<>())
                        .getOrDefault(employee, context.findShiftByArray(new int[24]));

                daysWithShiftsToSplit
                        .computeIfAbsent(employee, k -> new HashMap<>())
                        .put(date, shift);
            }
            LinkedHashMap<Employee, Map<LocalDate, Shift>> sortedData = daysWithShiftsToSplit.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> context.getWorkingDaysCount().getOrDefault(entry.getKey(), 0)))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().entrySet().stream()
                                    .sorted((a, b) -> context.getShiftLength(b.getValue()).compareTo(context.getShiftLength(a.getValue())))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new)),
                            (v1, v2) -> v1,
                            LinkedHashMap::new
                    ));

            for (Map.Entry<Employee, Map<LocalDate, Shift>> entry : sortedData.entrySet()) {
                Employee employee = entry.getKey();
                Map<LocalDate, Shift> employeeDateShift = entry.getValue();

                List<ShiftSwapCandidate> swapCandidates = employeeDateShift.entrySet().stream()
                        .flatMap(insideEntry -> {
                            LocalDate insideDate = insideEntry.getKey();
                            Shift insideShift = insideEntry.getValue();

                            return others.stream()
                                    .filter(empl -> !empl.equals(employee))
                                    .filter(sortedData::containsKey)
                                    .filter(empl -> !context.employeeIsWorking(empl, insideDate))
                                    .flatMap(empl -> sortedData.get(empl).entrySet().stream()
                                            .filter(otherEmplEntry -> {
                                                LocalDate otherDate = otherEmplEntry.getKey();
                                                Shift otherShift = otherEmplEntry.getValue();
                                                return insideShift.equals(otherShift) && !context.employeeIsWorking(employee, otherDate)
                                                        && context.getShiftLength(insideShift).compareTo(BigDecimal.valueOf(9)) > 0;
                                            })
                                            .map(otherEmplEntry -> new ShiftSwapCandidate(employee, empl, insideDate, otherEmplEntry.getKey(), insideShift)));
                        }).toList();

                for (ShiftSwapCandidate candidate : swapCandidates) {
                    Employee originalEmployee = candidate.originalEmployee();
                    Employee otherEmployeeForSwap = candidate.employeeForSwapShift();
                    LocalDate originalEmployeeDate = candidate.originalDateForSwap();
                    LocalDate otherEmployeeDateForSwap = candidate.otherEmployeeDateForSwap();

                    if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= wantedMaxWorkingDays) break;
                    if (context.getWorkingDaysCount().getOrDefault(otherEmployeeForSwap, 0) >= wantedMaxWorkingDays) continue;

                    if (context.employeeIsWorking(otherEmployeeForSwap, originalEmployeeDate)) continue;
                    if (context.employeeIsWorking(originalEmployee, otherEmployeeDateForSwap)) continue;

                    if (context.employeeIsOnVacation(originalEmployee,originalEmployeeDate)) continue;
                    if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;
                    if (context.employeeIsOnVacation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                    if (context.employeeIsOnVacation(originalEmployee,otherEmployeeDateForSwap)) continue;

                    if (context.employeeIsOnDelegation(originalEmployee,originalEmployeeDate)) continue;
                    if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;
                    if (context.employeeIsOnDelegation(otherEmployeeForSwap,originalEmployeeDate)) continue;
                    if (context.employeeIsOnDelegation(originalEmployee,otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingInWarehouse(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingInWarehouse(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isOpeningOrClosingStore(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isOpeningOrClosingStore(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingOnCheckout(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingOnCheckout(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeWorkingOnCredit(originalEmployee, originalEmployeeDate)) continue;
                    if (context.isEmployeeWorkingOnCredit(otherEmployeeForSwap, otherEmployeeDateForSwap)) continue;

                    if (context.isEmployeeOnRestRequirementDayOff(originalEmployee, otherEmployeeDateForSwap)) continue;
                    if (context.isEmployeeOnRestRequirementDayOff(otherEmployeeForSwap, originalEmployeeDate)) continue;

                    Shift currentShiftOnDate = context.getFinalSchedule()
                            .getOrDefault(originalEmployeeDate, new HashMap<>())
                            .get(originalEmployee);

                    if (currentShiftOnDate == null) continue;
                    if (context.getShiftLength(currentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                    Shift otherCurrentShiftOnDate = context.getFinalSchedule()
                            .getOrDefault(otherEmployeeDateForSwap, new HashMap<>())
                            .get(otherEmployeeForSwap);

                    if (otherCurrentShiftOnDate == null) continue;
                    if (context.getShiftLength(otherCurrentShiftOnDate).compareTo(BigDecimal.valueOf(10)) < 0) continue;

                    if (isWeekendOrHoliday(candidate.originalDateForSwap()) || isWeekendOrHoliday(candidate.otherEmployeeDateForSwap())) continue;

                    DividedShiftDTO dividedShiftDTO = divideShift(currentShiftOnDate, context);

                    Shift shiftForOriginal = originalEmployee.isCashier() ? dividedShiftDTO.afternoonShift() : dividedShiftDTO.morningShift();
                    Shift shiftForOther    = originalEmployee.isCashier() ? dividedShiftDTO.morningShift()   : dividedShiftDTO.afternoonShift();

                    context.updateShiftOnSchedule(candidate.originalDateForSwap(), originalEmployee, shiftForOriginal);
                    context.registerShiftOnSchedule(candidate.originalDateForSwap(), otherEmployeeForSwap, shiftForOther, candidate.originalDateForSwap().getDayOfWeek());

                    context.updateShiftOnSchedule(candidate.otherEmployeeDateForSwap(), otherEmployeeForSwap, shiftForOther);
                    context.registerShiftOnSchedule(candidate.otherEmployeeDateForSwap(), originalEmployee, shiftForOriginal, candidate.otherEmployeeDateForSwap().getDayOfWeek());

                    anySwapDone = true;
                    break;
                }
                if (anySwapDone) break;
            }
        }
        return anySwapDone;
    }

    private boolean isWeekendOrHoliday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date);
    }

    private DividedShiftDTO divideShift(Shift shift, ScheduleGeneratorContext context){
        int startHour = shift.getStartHour().getHour();
        int startMinute = shift.getStartHour().getMinute();

        int endHour = shift.getEndHour().getHour();
        int endMinute = shift.getEndHour().getMinute();

        int midHour = (startHour + endHour) / 2;

        if (midHour < 13) midHour = 13;
        if (midHour > 16) midHour = 16;

        Shift morningShift = context.findShiftByHours(LocalTime.of(startHour, startMinute), LocalTime.of(midHour, 0));
        Shift afternoonShift = context.findShiftByHours(LocalTime.of(midHour, 0), LocalTime.of(endHour, endMinute));

        return new DividedShiftDTO(
                morningShift,
                afternoonShift
        );
    }

}