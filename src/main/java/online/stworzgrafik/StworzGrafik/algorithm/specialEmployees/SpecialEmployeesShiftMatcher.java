package online.stworzgrafik.StworzGrafik.algorithm.specialEmployees;

import com.mysql.cj.log.Log;
import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseHoursForEmployeeIndexDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

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
        log.info("PRACOWNIK SPECJALNY - TWORZENIE INDYWIDUALNEGO GRAFIKA");

        List<Employee> specialActiveEmployees = context.getStoreAllActiveEmployees().stream()
                .filter(Employee::getIsSpecial)
                .toList();

        for (Employee special : specialActiveEmployees) {
            assignWeekends(context, special);
            assignRestRequirementDayOff(context, special);
            assignWorkToWeekDays(context,special);
        }
    }

    private void assignWorkToWeekDays(ScheduleGeneratorContext context, Employee employee){
        YearMonth yearMonth = YearMonth.of(context.getYear(),context.getMonth());

        Map<String,Integer> shifts = shiftsUsedCount();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
            LocalDate date = LocalDate.of(context.getYear(),context.getMonth(),day);

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            if (context.isEmployeeOnRestRequirementDayOff(employee,date)) continue;
            if (context.employeeIsOnVacation(employee,date)) continue;
            if (context.employeeIsOnDelegation(employee,date)) continue;
            if (context.employeeIsWorking(employee,date)) continue;

            String shiftName = shifts.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("DRAFT");

            switch (shiftName){
                case ("MORNING") -> {
                    Shift morningShift = getMorningShift(context,date,employee);
                    context.registerShiftOnSchedule(date,employee,morningShift,date.getDayOfWeek());

                    shifts.merge("MORNING",1,Integer::sum);
                }
                case ("AFTERNOON") -> {
                    Shift afternoonShift = getAfternoonShift(context,date,employee);
                    context.registerShiftOnSchedule(date,employee,afternoonShift,date.getDayOfWeek());

                    shifts.merge("AFTERNOON",1,Integer::sum);
                }
                case ("DRAFT") -> {
                    Shift draftShift = calculateHighestDraftHoursShift(context,employee,date);
                    context.registerShiftOnSchedule(date,employee,draftShift,date.getDayOfWeek());

                    shifts.merge("DRAFT",1,Integer::sum);
                }
            }
        }
    }

    private Shift getAfternoonShift(ScheduleGeneratorContext context, LocalDate date, Employee employee){
        OpenCloseHoursForEmployeeIndexDTO hoursDto = context.getStoreOpenCloseHoursForEmployeesByDate().getOrDefault(date, new OpenCloseHoursForEmployeeIndexDTO(8, 21));

        int employeeMaxWorkingHours = employee.getSpecialWorkNorm().getMaxDailyHours().intValue();

        LocalTime startHour = LocalTime.of(hoursDto.closeHour() - employeeMaxWorkingHours,0);
        LocalTime endHour = LocalTime.of(hoursDto.closeHour(),0);

        return context.findShiftByHours(startHour,endHour);
    }

    private Shift getMorningShift(ScheduleGeneratorContext context, LocalDate date, Employee employee){
        OpenCloseHoursForEmployeeIndexDTO hoursDto = context.getStoreOpenCloseHoursForEmployeesByDate().getOrDefault(date, new OpenCloseHoursForEmployeeIndexDTO(8, 21));

        int employeeMaxWorkingHours = employee.getSpecialWorkNorm().getMaxDailyHours().intValue();

        LocalTime startHour = LocalTime.of(hoursDto.openHour(),0);
        LocalTime endHour = LocalTime.of(hoursDto.openHour() +  employeeMaxWorkingHours,0);

        return context.findShiftByHours(startHour,endHour);
    }

    private Map<String,Integer> shiftsUsedCount(){
        Map<String,Integer> shifts = new HashMap<>();
        shifts.put("MORNING",0);
        shifts.put("AFTERNOON",0);
        shifts.put("DRAFT",0);

        return shifts;
    }

    private void assignRestRequirementDayOff(ScheduleGeneratorContext context, Employee employee) {
        Map<Integer, PeriodDateDTO> periodWeek = context.getPeriodWeek();
        for (Map.Entry<Integer,PeriodDateDTO> entry : periodWeek.entrySet()) {
            PeriodDateDTO periodDateDTO = entry.getValue();

            if (isEmployeeWorkingOnSaturdayInPeriodWeek(context,periodDateDTO,employee)){
                if (employeeAssignedToVacationOrZeroDraftDay(context, periodDateDTO,employee)) continue;

                LinkedHashMap<LocalDate,Double> weekDaysScoring = calculateWeekDaysScoring(context,periodDateDTO);
                for (LocalDate date : weekDaysScoring.keySet()){
                    if (!context.employeeHasProposalShift(employee,date)){
                        context.assignEmployeeToRestRequirementDayOff(employee,date);
                        break;
                    }
                }
            }
            else {
                context.assignEmployeeToRestRequirementDayOff(employee,saturdayInPeriodWeek(context,periodDateDTO));
            }
        }
    }

    private LinkedHashMap<LocalDate,Double> calculateWeekDaysScoring(ScheduleGeneratorContext context, PeriodDateDTO dto){
        LocalDate periodStartDate = dto.startDate();
        LocalDate periodEndDate  = dto.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        LinkedHashMap<LocalDate,Double> daysScoring = new LinkedHashMap<>();
        for (int day = periodStartDayOfMonth; day <= periodEndDayOfMonth; day++){
            LocalDate currentDate = LocalDate.of(periodStartDate.getYear(), periodStartDate.getMonth(),day);

            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

            double vacationAndDaysOffValue = 0.00;
            for (Employee employee : context.getStoreAllActiveEmployees()){
                if (context.employeeIsOnVacation(employee,currentDate)){
                    vacationAndDaysOffValue += 2.00;
                }

                if (context.employeeHasProposalDaysOff(employee,currentDate)){
                    vacationAndDaysOffValue += 2.00;
                }
            }

            double divideBy = 8;
            double dailySum = Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(currentDate, new int[24])).sum();
            double dailyScoring = (dailySum / divideBy) + vacationAndDaysOffValue;

            daysScoring.put(currentDate,dailyScoring);
        }
        return daysScoring.entrySet().stream()
                .sorted(Comparator.comparingDouble((Map.Entry::getValue)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private boolean employeeAssignedToVacationOrZeroDraftDay(ScheduleGeneratorContext context, PeriodDateDTO periodDateDTO, Employee employee){
        LocalDate periodStartDate = periodDateDTO.startDate();
        LocalDate periodEndDate = periodDateDTO.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        for (int day = periodStartDayOfMonth; day <= periodEndDayOfMonth; day++) {
            LocalDate date = LocalDate.of(context.getYear(), context.getMonth(), day);

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            if (context.employeeHasProposalShift(employee,date)) continue;

            if (context.employeeIsOnVacation(employee,date)){
                context.assignEmployeeToRestRequirementDayOff(employee,date);
                return true;
            }

            if (checkZeroDraftRequirementDay(context,date)){
                context.assignEmployeeToRestRequirementDayOff(employee,date);
                return true;
            }
        }

        return false;
    }

    private static boolean checkZeroDraftRequirementDay(ScheduleGeneratorContext context, LocalDate date) {
        int dailyStoreDraftCount = Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(date, new int[24])).sum();

        return dailyStoreDraftCount == 0;
    }

    private void assignWeekends(ScheduleGeneratorContext context, Employee employee) {
        log.info("WEEKENDY");
        LinkedHashMap<LocalDate, Double> weekendsScoring = calculateWeekendsScoring(context);

        int workingWeekends = 0;
        int dayOffProposalsCount = weekDaysCountWithDayOffProposal(context,employee);
        int maxWorkingWeekends = Math.max(dayOffProposalsCount, 2);
        log.info("workingWeekends={}, dayOffProposalsCount={}, maxWorkingWeekends={}",
                workingWeekends,
                dayOffProposalsCount,
                maxWorkingWeekends
                );


        for (LocalDate weekend : weekendsScoring.keySet()){
            log.info("[PETLA] sobota={}", weekend);
            if (workingWeekends >= maxWorkingWeekends) break;

            if (context.employeeIsOnDelegation(employee,weekend) || context.employeeHasProposalShift(employee,weekend)) {
                log.info("[PETLA] delegation CONTINUE");
                workingWeekends += 1;
                continue;
            }

            if (!context.employeeHasProposalDaysOff(employee,weekend)) {

                Shift highestDraftShift = calculateHighestDraftHoursShift(context, employee, weekend);
                log.info("[PETLA] brak proposal day off register | shift {}-{}", highestDraftShift.getStartHour(),highestDraftShift.getEndHour());

                context.registerShiftOnSchedule(weekend, employee, highestDraftShift, weekend.getDayOfWeek());

                workingWeekends += 1;
            }
        }
    }

    private boolean isEmployeeWorkingOnSaturdayInPeriodWeek(ScheduleGeneratorContext context, PeriodDateDTO periodDateDTO, Employee employee){
        LocalDate periodStartDate = periodDateDTO.startDate();
        LocalDate periodEndDate = periodDateDTO.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        for (int day = periodStartDayOfMonth; day <= periodEndDayOfMonth; day++){
            LocalDate date = LocalDate.of(context.getYear(),context.getMonth(),day);

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY && context.employeeIsWorking(employee,date)){
                return true;
            }
        }
        return false;
    }

    private LocalDate saturdayInPeriodWeek(ScheduleGeneratorContext context, PeriodDateDTO periodDateDTO){
        LocalDate periodStartDate = periodDateDTO.startDate();
        LocalDate periodEndDate = periodDateDTO.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        for (int day = periodStartDayOfMonth; day <= periodEndDayOfMonth; day++){
            LocalDate date = LocalDate.of(context.getYear(),context.getMonth(),day);

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY){
                return date;
            }
        }

        return LocalDate.of(2000,1,1);
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
