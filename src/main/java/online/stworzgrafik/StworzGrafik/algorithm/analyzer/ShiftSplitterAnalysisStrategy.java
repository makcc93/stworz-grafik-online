package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftSplitterAnalysisStrategy implements ScheduleAnalysisStrategy {
    private final CalendarCalculation calendarCalculation;
    private final HolidayManager holidayManager;

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.SHIFT_SPLITTER;
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
        int monthlyMaxWorkingDays = ((ShiftSplitterAnalysisResult) result).monthlyMaxWorkingDays();

        while (context.getWorkingDaysCount().entrySet().stream()
                .anyMatch(e -> e.getValue() <= monthlyMaxWorkingDays - 2)) {
            boolean resolved = splitShifts(context);

            if (!resolved) break;
        }
    }

    private boolean splitShifts(ScheduleGeneratorContext context){
        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        boolean result = managersSplitShifts(context,monthlyMaxWorkingDays);
        result = creditEmployeesSplitShifts(context, monthlyMaxWorkingDays);
        result = otherEmployeesSplitShifts(context,monthlyMaxWorkingDays);

        return result;
    }

    private boolean managersSplitShifts(ScheduleGeneratorContext context, int monthlyMaxWorkingDays) {
        return splitShiftsByCriteria(context, monthlyMaxWorkingDays,
                Employee::isCanOpenCloseStore);
    }

    private boolean creditEmployeesSplitShifts(ScheduleGeneratorContext context, int monthlyMaxWorkingDays) {
        return splitShiftsByCriteria(context, monthlyMaxWorkingDays,
                empl -> empl.isCanOperateCredit() && !empl.isCanOpenCloseStore());
    }

    private boolean otherEmployeesSplitShifts(ScheduleGeneratorContext context, int monthlyMaxWorkingDays) {
        return splitShiftsByCriteria(context, monthlyMaxWorkingDays,
                empl -> !empl.isCanOpenCloseStore()
                        && !empl.isCanOperateCredit()
//                        && !empl.isCashier()
                        && !empl.isWarehouseman());
    }

    private boolean splitShiftsByCriteria(ScheduleGeneratorContext context, int monthlyMaxWorkingDays, Predicate<Employee> employeeFilter) {
        boolean anySwapDone = false;
        int wantedMaxWorkingDays = monthlyMaxWorkingDays -1;

        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(employeeFilter)
                .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                .toList();

        Map<Employee, Map<LocalDate, Shift>> daysWithShiftsToSplit = new HashMap<>();
        YearMonth yearMonth = YearMonth.of(context.getYear(), context.getMonth());

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);
            for (Employee employee : employees) {
                if (!context.employeeIsWorking(employee, date)) continue;
                if (context.employeeIsOnVacation(employee, day)) continue;
                if (context.employeeHasProposalShift(employee, date)) continue;
                if (context.employeeHasProposalDaysOff(employee, date)) continue;
                if (context.employeeIsOnDayOff(employee, day)) continue;
                if (context.employeeIsInWarehouse(employee,date)) continue;

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
                                .sorted((a, b) -> getShiftLength(b.getValue()) - getShiftLength(a.getValue()))
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

                        return employees.stream()
                                .filter(empl -> !empl.equals(employee))
                                .filter(sortedData::containsKey)
                                .filter(empl -> !context.employeeIsWorking(empl, insideDate))
                                .flatMap(empl -> sortedData.get(empl).entrySet().stream()
                                        .filter(otherEmplEntry -> {
                                            LocalDate otherDate = otherEmplEntry.getKey();
                                            Shift otherShift = otherEmplEntry.getValue();
                                            return insideShift.equals(otherShift) && !context.employeeIsWorking(employee, otherDate)
                                                    && getShiftLength(insideShift) > 9;
                                        })
                                        .map(otherEmplEntry -> new ShiftSwapCandidate(employee, empl, insideDate, otherEmplEntry.getKey(), insideShift)));
                    }).toList();

            for (ShiftSwapCandidate candidate : swapCandidates) {
                Employee originalEmployee = candidate.originalEmployee();
                Employee otherEmployeeForSwap = candidate.employeeForSwapShift();
                LocalDate originalEmployeeDate = candidate.originalDateForSwap();
                LocalDate otherEmployeeDateForSwap = candidate.otherEmployeeDateForSwap();

                // re-walidacja dni
                if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= wantedMaxWorkingDays) break;
                if (context.getWorkingDaysCount().getOrDefault(otherEmployeeForSwap, 0) >= wantedMaxWorkingDays) continue;

                // re-walidacja kolizji po poprzednich swapach
                if (context.employeeIsWorking(otherEmployeeForSwap, originalEmployeeDate)) continue;
                if (context.employeeIsWorking(originalEmployee, otherEmployeeDateForSwap)) continue;

                if (context.employeeIsInWarehouse(originalEmployee,originalEmployeeDate)) continue;
                if (context.employeeIsInWarehouse(otherEmployeeForSwap,otherEmployeeDateForSwap)) continue;

                // re-walidacja zmiany — pobierz AKTUALNĄ zmianę z contextu, nie z sortedData
                Shift currentShiftOnDate = context.getFinalSchedule()
                        .getOrDefault(originalEmployeeDate, new HashMap<>())
                        .get(originalEmployee);

                if (currentShiftOnDate == null) continue;
                if (getShiftLength(currentShiftOnDate) < 10) continue;

                // re-walidacja zmiany otherEmployee
                Shift otherCurrentShiftOnDate = context.getFinalSchedule()
                        .getOrDefault(otherEmployeeDateForSwap, new HashMap<>())
                        .get(otherEmployeeForSwap);

                if (otherCurrentShiftOnDate == null) continue;
                if (getShiftLength(otherCurrentShiftOnDate) < 10) continue;

                // divide na aktualnej zmianie, nie candidate.swappingShift()
                if (processCandidate(candidate, context, monthlyMaxWorkingDays, currentShiftOnDate)) {
                    anySwapDone = true;
                    break;
                }
            }
        }
        return anySwapDone;
    }

    private boolean processCandidate(ShiftSwapCandidate candidate, ScheduleGeneratorContext context, int maxDays, Shift currentShift) {
        Employee originalEmployee = candidate.originalEmployee();
        Employee otherEmployee = candidate.employeeForSwapShift();

        if (context.getWorkingDaysCount().getOrDefault(originalEmployee, 0) >= maxDays) return false;
        if (context.getWorkingDaysCount().getOrDefault(otherEmployee, 0) >= maxDays) return false;

        if (isWeekendOrHoliday(candidate.originalDateForSwap()) || isWeekendOrHoliday(candidate.otherEmployeeDateForSwap())) {
            return false;
        }

//        List<Shift> dividedShift = divideShift(currentShift, context);
        DividedShiftDTO dividedShiftDTO = divideShift(currentShift, context);

        Shift shiftForOriginal = originalEmployee.isCashier() ? dividedShiftDTO.afternoonShift() : dividedShiftDTO.morningShift();
        Shift shiftForOther    = originalEmployee.isCashier() ? dividedShiftDTO.morningShift()   : dividedShiftDTO.afternoonShift();

        context.updateShiftOnSchedule(candidate.originalDateForSwap(), originalEmployee, shiftForOriginal);
        context.registerShiftOnSchedule(candidate.originalDateForSwap(), otherEmployee, shiftForOther, candidate.originalDateForSwap().getDayOfWeek());

        context.updateShiftOnSchedule(candidate.otherEmployeeDateForSwap(), otherEmployee, shiftForOther);
        context.registerShiftOnSchedule(candidate.otherEmployeeDateForSwap(), originalEmployee, shiftForOriginal, candidate.otherEmployeeDateForSwap().getDayOfWeek());

        return true;
    }

    private boolean isWeekendOrHoliday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date);
    }

    private DividedShiftDTO divideShift(Shift shift, ScheduleGeneratorContext context){
        int startHour = shift.getStartHour().getHour();
        int endHour = shift.getEndHour().getHour();

        int midHour = (startHour + endHour) / 2;

        if (midHour < 13) midHour = 13;
        if (midHour > 16) midHour = 16;

        Shift morningShift = context.findShiftByHours(LocalTime.of(startHour, 0), LocalTime.of(midHour, 0));
        Shift afternoonShift = context.findShiftByHours(LocalTime.of(midHour, 0), LocalTime.of(endHour, 0));
        log.info("*** Podzielona zmana: {}-{} na zmiany: 1. {}-{}, 2. {}-{}", shift.getStartHour(),shift.getEndHour(),morningShift.getStartHour(),morningShift.getEndHour(),afternoonShift.getStartHour(),afternoonShift.getEndHour());

        return new DividedShiftDTO(
                morningShift,
                afternoonShift
        );
    }


//    private List<Shift> divideShift(Shift shift, ScheduleGeneratorContext context){
//        int startHour = shift.getStartHour().getHour();
//        int endHour = shift.getEndHour().getHour();
//
//        int midHour = (startHour + endHour) / 2;
//
//        if (midHour < 13) midHour = 13;
//        if (midHour > 16) midHour = 16;
//
//        Shift firstShift = context.findShiftByHours(LocalTime.of(startHour, 0), LocalTime.of(midHour, 0));
//        Shift secondShift = context.findShiftByHours(LocalTime.of(midHour, 0), LocalTime.of(endHour, 0));
//        log.info("*** Podzielona zmana: {}-{} na zmiany: 1. {}-{}, 2. {}-{}", shift.getStartHour(),shift.getEndHour(),firstShift.getStartHour(),firstShift.getEndHour(),secondShift.getStartHour(),secondShift.getEndHour());
//
//        return List.of(firstShift,secondShift);
//    }

    private int getShiftLength(Shift shift){
        return shift.getEndHour().getHour() - shift.getStartHour().getHour();
    }
}