package online.stworzgrafik.StworzGrafik.algorithm.specialEmployees;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SpecialEmployeesShiftMatcher {
    private final HolidayManager holidayManager;

    public void proceed(ScheduleGeneratorContext context) {
        log.info("PRACOWNIK SPECJALNY - TWORZENIA INDYWIDUALNEGO GRAFIKA");

        List<Employee> specialActiveEmployees = context.getStoreAllActiveEmployees().stream()
                .filter(Employee::getIsSpecial)
                .toList();

        for (Employee special : specialActiveEmployees) {
            assignWeekends(context, special);
            assignRestRequirementDayOff(context, special);
            assignWeekDays(context);
        }
    }

    private void assignRestRequirementDayOff(ScheduleGeneratorContext context, Employee special) {
        //todo
    }

    private void assignWeekends(ScheduleGeneratorContext context, Employee employee) {
        LinkedHashMap<LocalDate, Double> weekendsScoring = calculateWeekendsScoring(context);

        int workingWeekends = 0;
        int dayOffProposalsCount = weekDaysCountWithDayOffProposal(context,employee);
        int maxWorkingWeekends = Math.max(dayOffProposalsCount, 2);

        for (LocalDate weekend : weekendsScoring.keySet()){
            if (workingWeekends >= maxWorkingWeekends) break;

            if (context.employeeIsOnDelegation(employee,weekend) || context.employeeHasProposalShift(employee,weekend)) {
                workingWeekends += 1;
                continue;
            }

            if (!context.employeeHasProposalDaysOff(employee,weekend)) {
                Shift highestDraftShift = calculateHighestDraftHoursShift(context, employee, weekend);
                context.registerShiftOnSchedule(weekend, employee, highestDraftShift, weekend.getDayOfWeek());

                workingWeekends += 1;
            }
        }
    }

    private int weekDaysCountWithDayOffProposal(ScheduleGeneratorContext context, Employee employee){
        YearMonth yearMonth = YearMonth.of(context.getYear(),context.getMonth());

        int dayOffProposalsCount = 0;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = yearMonth.atDay(day);
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY || holidayManager.isHoliday(date)) continue;

            if (context.employeeHasProposalDaysOff(employee,date)) dayOffProposalsCount += 1;
        }
        return dayOffProposalsCount;
    }

    private Shift calculateHighestDraftHoursShift(ScheduleGeneratorContext context, Employee employee, LocalDate date) {
        int[] dailyDraft = context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24]);
        int maxDailyHours = employee.getSpecialWorkNorm().getMaxDailyHours().intValue();

        int bestEndIndex = maxDailyHours - 1;
        int bestValue = -1;

        for (int end = maxDailyHours - 1; end < 24; end++) {
            int start = end - maxDailyHours + 1;
            int value = 0;
            for (int idx = start; idx <= end; idx++) {
                value += dailyDraft[idx];
            }
            if (value > bestValue) {
                bestValue = value;
                bestEndIndex = end;
            }
        }

        int startIndex = bestEndIndex - maxDailyHours + 1;
        LocalTime startHour = LocalTime.of(startIndex, 0);
        LocalTime endHour = (bestEndIndex + 1 == 24)
                ? LocalTime.MIDNIGHT
                : LocalTime.of(bestEndIndex + 1, 0);

        return context.findShiftByHours(startHour, endHour);
    }

    private static LinkedHashMap<LocalDate, Double> calculateWeekendsScoring(ScheduleGeneratorContext context) {
        Map<Integer, PeriodDateDTO> periodWeek = context.getPeriodWeek();
        List<Employee> employees = context.getStoreAllActiveEmployees().stream()
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !empl.isPok())
                .toList();

        Map<LocalDate, Double> weekendScoring = new HashMap<>();

        for (Map.Entry<Integer, PeriodDateDTO> entry : periodWeek.entrySet()) {
            Integer weekIndex = entry.getKey();
            PeriodDateDTO periodDateDTO = entry.getValue();

            LocalDate periodStartDate = periodDateDTO.startDate();
            LocalDate periodEndDate = periodDateDTO.endDate();

            int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
            int periodEndDayOfMonth = periodEndDate.getDayOfMonth();


            for (int day = (periodStartDayOfMonth + 1); day < periodEndDayOfMonth; day++) {
                LocalDate currentDate = LocalDate.of(periodStartDate.getYear(), periodStartDate.getMonth(), day);

                if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY) continue;

                double vacationAndDaysOffValue = 0.00;
                for (Employee employee : employees) {
                    if (context.employeeIsOnVacation(employee, currentDate)) {
                        vacationAndDaysOffValue += 2.00;
                    }

                    if (context.employeeHasProposalDaysOff(employee, currentDate)) {
                        vacationAndDaysOffValue += 2.00;
                    }

                    if (employeeHasDayOffProposalInWeekBeforeSaturday(context,employee,currentDate)){
                        vacationAndDaysOffValue += 999999.00;
                    }
                }

                double divideBy = 8;
                double dailySum = Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(currentDate, new int[24])).sum();
                double dailyScoring = (dailySum / divideBy) + vacationAndDaysOffValue;

                weekendScoring.put(currentDate, dailyScoring);
            }

        }
        return weekendScoring.entrySet().stream()
                .sorted(Comparator.comparingDouble(
                        ((Map.Entry<LocalDate, Double> entry) -> entry.getValue())
                ).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private static boolean employeeHasDayOffProposalInWeekBeforeSaturday(ScheduleGeneratorContext context, Employee employee, LocalDate currentDate) {
        for (int i = 5; i >= 0; i--){
            if (context.employeeHasProposalDaysOff(employee,currentDate.minusDays(5))){
                return true;
            }
        }
        return false;
    }
}
