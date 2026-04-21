package online.stworzgrafik.StworzGrafik.algorithm;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.AnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseStoreHoursDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;
    private final CalendarCalculation calendarCalculation;
    private final ScheduleAnalyzer scheduleAnalyzer;

    public void matchEmployeeToShift(ScheduleGeneratorContext context) {
        Map<LocalDate, int[]> originalStoreDrafts = context.getUneditedOriginalDateStoreDraft();
        LinkedHashMap<LocalDate, int[]> everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc = context.getEveryDayStoreDemandDraftWorkingOn();

        Map<LocalDate, List<Shift>> generatedShiftsByDate = context.getGeneratedShiftsByDay();
        List<Employee> storeActiveEmployees = context.getStoreActiveEmployees();

        for (Map.Entry<LocalDate,int[]> entry : everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc.entrySet()) {
            LocalDate date = entry.getKey();

            int[] uneditedOriginalStoreDailyDraft = originalStoreDrafts.get(date);

            log.info("\noryginalny draft w dniu {}      - {}", date, uneditedOriginalStoreDailyDraft);

            if (dayIsHolidayOrHasEmptyDemandDraft(date, everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc)) {
                log.info("Pomijam dzień {} ponieważ jest świętem lub brak ustalonego zapotrzebowania na pracę", date);

                continue;
            }

            List<Employee> availableEmployees = getAvailableEmployees(context, storeActiveEmployees, date);
            List<Shift> shiftsSorted = getShiftsSortedByStartHour(generatedShiftsByDate, date);

            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees,AnalyzeType.TOO_MANY_DAY_OFF_PROPOSALS);
            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees, AnalyzeType.TOO_MANY_SHIFT_PROPOSALS);
            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees,AnalyzeType.MANAGER_OPENING_HOUR);
            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees,AnalyzeType.MANAGER_CLOSING_HOUR);

            if (!morningOpenStoreEmployeeAlreadyInSchedule(context,date,uneditedOriginalStoreDailyDraft)) {
                applyOpenStoreEmployee(context, date, availableEmployees, shiftsSorted);
            }

            if (!afternoonCloseStoreEmployeeAlreadyInSchedule(context,date,uneditedOriginalStoreDailyDraft)) {
                applyCloseStoreEmployee(context, date, availableEmployees, shiftsSorted);
            }

            applyCashierIfPresent(context, date, availableEmployees, shiftsSorted);

            if (!morningCreditEmployeeAlreadyInSchedule(context,date,uneditedOriginalStoreDailyDraft)){
                applyMorningCreditEmployee(context, availableEmployees, shiftsSorted, date);
            }

            if (!afternoonCreditEmployeeAlreadyInSchedule(context,date,uneditedOriginalStoreDailyDraft)){
                applyAfternoonCreditEmployee(context, availableEmployees, shiftsSorted, date);
            }

            while (!shiftsSorted.isEmpty()) {
                Optional<Shift> shift = shiftsSorted.stream().min(longestShift());

                if (shift.isEmpty()) {
                    log.info("Brak dostępnych zmian do rozdysponowania w dniu {}", date);
                    context.registerMessageOnSchedule(
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.INFO,
                                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                                    "Brak dostępnych zmian w dniu: " + date,
                                    null,
                                    date)
                    );
                    break;
                }

                Optional<Employee> employee = availableEmployees.stream()
                        .filter(empl -> !empl.isCashier())
                        .filter(empl -> !empl.isWarehouseman())
                        .sorted(sortByWorkedHoursAndSpecialSortForWeekends(context, date))
                        .findFirst();


                if (employee.isEmpty()) {
                    log.info("Brak dostępnych pracowników w dniu {}", date);
                    context.registerMessageOnSchedule(
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.INFO,
                                    ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                                    "Brak dostępnych pracowników w dniu: " + date,
                                    null,
                                    date
                            )
                    );
                    break;
                }

                whenEmployeeHoursExceeded(context, date, employee.get());
                whenEmployeeWorkingDaysExceeded(context, date, employee.get());

                context.registerShiftOnSchedule(date, employee.get(), shift.get(), date.getDayOfWeek());
                shiftsSorted.remove(shift.get());
                availableEmployees.remove(employee.get());
            }
            while (shiftsSorted.size() > availableEmployees.size()){
                log.warn("Mamy więcej zmian niż pracowników w dniu {} - wdrażam działanie", date);
                scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees,AnalyzeType.UNDERSTAFFED);
            }
        }
    }

    private void showShiftsInLog(List<Shift> shifts){
        String shiftsAsString = shifts.stream()
                .map(shift -> shift.getStartHour() + "-" + shift.getEndHour())
                .collect(Collectors.joining(" | "));

        log.info("Zmiany: {}", shiftsAsString);
    }

    private static Comparator<Shift> longestShift() {
        return Comparator.comparingInt(
                (Shift s) -> s.getEndHour().getHour() - s.getStartHour().getHour()
        ).reversed();
    }

    private boolean dayIsHolidayOrHasEmptyDemandDraft(LocalDate day, Map<LocalDate, int[]> everyDayStoreDemandDraftAfterProposals) {
        return holidayManager.isHoliday(day) ||
                Arrays.stream(everyDayStoreDemandDraftAfterProposals.getOrDefault(day, new int[24])).sum() == 0;
    }

    private ArrayList<Employee> getAvailableEmployees(ScheduleGeneratorContext context, List<Employee> storeActiveEmployees, LocalDate day) {
        return new ArrayList<>(storeActiveEmployees.stream()
                .filter(empl -> !context.employeeIsOnDayOff(empl, day.getDayOfMonth()))
                .filter(empl -> !context.employeeIsOnVacation(empl, day.getDayOfMonth()))
                .filter(empl -> !context.employeeHasProposalShift(empl, day))
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !context.isEmployeeWorkingInWarehouse(empl,day))
                .filter(empl ->
                        calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth()) > context.getWorkingDaysCount().getOrDefault(empl,0))
                .toList()
        );
    }

    private static ArrayList<Shift> getShiftsSortedByStartHour(Map<LocalDate, List<Shift>> generatedShiftsByDate, LocalDate day) {
        return new ArrayList<>(generatedShiftsByDate.getOrDefault(day, Collections.emptyList()).stream()
                .sorted(Comparator.comparingInt(
                        shift -> shift.getStartHour().getHour()
                ))
                .toList()
        );
    }

    private boolean afternoonCloseStoreEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, Shift> employeeShift = context.getFinalSchedule().getOrDefault(day, new HashMap<>());
        for (Map.Entry<Employee, Shift> entry : employeeShift.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOpenCloseStore()) continue;

            int[] shiftAsArray = context.shiftAsArray(entry.getValue());

            int lastHourIndex = 23;
            for (int i = 23; i >= 0; i--){
                if (dailyDraft[i] > 0){
                    lastHourIndex = i;
                    break;
                }
            }

            if (shiftAsArray[lastHourIndex] > 0) return true;
        }

        return false;
    }

    private void applyAfternoonCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate date) {
        Optional<Employee> employeeToOperateAfternoonCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .sorted(sortByWorkedHoursAndSpecialSortForWeekends(context, date))
                .findFirst();

        if (employeeToOperateAfternoonCredit.isEmpty()){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego sprzedaż ratalną popołudniu w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Optional<Shift> afternoonCreditShift = shiftsSorted.stream().min(longestCloseStoreShift());

        if (afternoonCreditShift.isEmpty()){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej popołudniowej zmiany do zamknięcia w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Employee employeeToOperateCredit = employeeToOperateAfternoonCredit.get();
        Shift creditShift = afternoonCreditShift.get();

        whenEmployeeHoursExceeded(context, date, employeeToOperateCredit);
        whenEmployeeWorkingDaysExceeded(context, date, employeeToOperateCredit);

        context.registerShiftOnSchedule(date, employeeToOperateCredit, creditShift,date.getDayOfWeek());
        context.assignEmployeeToCredit(date,employeeToOperateCredit,creditShift);

        shiftsSorted.remove(creditShift);
        availableEmployees.remove(employeeToOperateCredit);
    }

    private void applyMorningCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate date) {
        Optional<Employee> employeeToOperateMorningCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .sorted(sortByWorkedHoursAndSpecialSortForWeekends(context, date))
                .findFirst();

        if (employeeToOperateMorningCredit.isEmpty()){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego sprzedaż ratalną rano w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Optional<Shift> morningCreditShift = shiftsSorted.stream().min(longestOpenStoreShift());

        if (morningCreditShift.isEmpty()){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej otwierającej zmiany w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Shift creditShift = morningCreditShift.get();
        Employee employeeToOperateCredit = employeeToOperateMorningCredit.get();

        whenEmployeeHoursExceeded(context, date, employeeToOperateCredit);
        whenEmployeeWorkingDaysExceeded(context, date, employeeToOperateCredit);

        context.registerShiftOnSchedule(date, employeeToOperateCredit,creditShift,date.getDayOfWeek());
        context.assignEmployeeToCredit(date, employeeToOperateCredit,creditShift);

        shiftsSorted.remove(creditShift);
        availableEmployees.remove(employeeToOperateCredit);
    }

    private boolean afternoonCreditEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, Shift> map = context.getFinalSchedule().getOrDefault(day, new HashMap<>());

        for (Map.Entry<Employee, Shift> entry : map.entrySet()) {
            Employee employee = entry.getKey();
            int[] employeeProposal = context.shiftAsArray(entry.getValue());

            for (int i = 23; i >= 0; i--){
                if (dailyDraft[i] > 0){
                    if (employee.isCanOperateCredit() && employeeProposal[i] > 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean morningOpenStoreEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, Shift> employeeShift = context.getFinalSchedule().getOrDefault(day,new HashMap<>());
        for (Map.Entry<Employee, Shift> entry : employeeShift.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOpenCloseStore()) continue;

            int[] shiftAsArray = context.shiftAsArray(entry.getValue());

            int openHourIndex = 0;
            for (int i = 0; i < 24; i++){
                if (dailyDraft[i] > 0){
                    openHourIndex = i;
                    break;
                }
            }

            if (shiftAsArray[openHourIndex] > 0) return true;
        }
        return false;
    }

    private boolean morningCreditEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, Shift> map = context.getFinalSchedule().getOrDefault(day, new HashMap<>());
        for (Map.Entry<Employee, Shift> entry : map.entrySet()) {
            Employee employee = entry.getKey();
            int[] employeeProposal = context.shiftAsArray(entry.getValue());

            for (int i = 0; i < dailyDraft.length; i++) {
                if (dailyDraft[i] > 0) {
                    if (employee.isCanOperateCredit() && employeeProposal[i] > 0 ||
                            (i + 1 < dailyDraft.length && employee.isCanOperateCredit() && employeeProposal[i + 1] > 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyCloseStoreEmployee(ScheduleGeneratorContext context, LocalDate date, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
       log.info("");
       log.info("");
       log.info("       C L O S E  S T O R E");

        Optional<Employee> employeeToCloseStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .findFirst();

        if (employeeToCloseStore.isEmpty()) {
            log.info("Brak dostępnego pracownika mogącego zamknąć sklep w dniu {}", date);
        }

        Optional<Shift> closeShift = shiftsSorted.stream().min(longestCloseStoreShift());

        if (closeShift.isEmpty()){
            log.info("Brak dostępnej zmiany do zamknięcia sklepu w dniu {}", date);
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej zamykącej zmiany w dniu: " + date,
                            null,
                            date
                    )
            );
        }

        if (employeeToCloseStore.isEmpty() || closeShift.isEmpty()) {
            modifyOpenStoreEmployeeHoursToAllDayShift(context, date);
            return;
        }

        Employee employeeClosingStore = employeeToCloseStore.get();
        Shift closingShift = closeShift.get();

        whenEmployeeHoursExceeded(context, date, employeeClosingStore);
        whenEmployeeWorkingDaysExceeded(context, date, employeeToCloseStore.get());

        context.registerShiftOnSchedule(date,employeeClosingStore,closingShift,date.getDayOfWeek());
        shiftsSorted.remove(closingShift);
        availableEmployees.remove(employeeClosingStore);
        log.info("");
        log.info("");
    }

    private void modifyOpenStoreEmployeeHoursToAllDayShift(ScheduleGeneratorContext context, LocalDate date){
        Employee employee = null;
        Shift shift = null;
        Map<Employee, Shift> dailySchedule = context.getFinalSchedule().getOrDefault(date, new HashMap<>());

        for (Employee e : dailySchedule.keySet()){
            if (e.isCanOpenCloseStore()){
                employee = e;

                shift = dailySchedule.get(e);
            }
        }

        if (employee == null){
            log.info("Brak w grafiku managera, który otwiera sklep. Dzień: {}",date);
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika otwierającego sklep w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        if (shift == null){
            log.info("Nie można znaleźć zmiany pracownika otwierającego sklep {} {} w dniu: {}",employee.getFirstName(),employee.getLastName(),date);
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć zmiany pracownika otwierającego sklep w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Shift newShiftFromOpenToClose = getShiftFromOpenToCloseStoreHours(context, date);

        context.updateShiftOnSchedule(date,employee,newShiftFromOpenToClose);

        context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                ScheduleMessageType.INFO,
                ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                "Z powodu braku pracownika mogącego zamknąć sklep w dniu: " + date +
                        " zmieniony został grafik " + employee.getFirstName() + " "
                        + employee.getLastName() + " na zmianę "
                        + newShiftFromOpenToClose.getStartHour()+ "-" + newShiftFromOpenToClose.getEndHour(),
                employee.getId(),
                date)
        );

        log.info("Z powodu braku pracownika mogącego zamnkąc w sklep w dniu {} zmieniony został grafik {} {} na całodniową zmianę {}-{}",
                date,
                employee.getFirstName(),
                employee.getLastName(),
                newShiftFromOpenToClose.getStartHour(),
                newShiftFromOpenToClose.getEndHour()
        );
    }

    private Shift getShiftFromOpenToCloseStoreHours(ScheduleGeneratorContext context, LocalDate date){
        OpenCloseStoreHoursDTO dto = context.getStoreOpenCloseHoursByDate(date);

        return context.findShiftByHours(LocalTime.of(dto.openHour(), 0), LocalTime.of(dto.closeHour(), 0));
    }

    private Comparator<Employee> employeeWithLowestHours(ScheduleGeneratorContext context, LocalDate date) {
        return (Comparator.comparingInt(
                (Employee empl) -> calculateHoursCountTwoDaysBeforeAndTwoDaysAfter(context, date, empl))
                .thenComparingInt(empl -> context.getEmployeeHours().getOrDefault(empl,0)));
    }

    private int calculateHoursCountTwoDaysBeforeAndTwoDaysAfter(ScheduleGeneratorContext context, LocalDate date, Employee empl) {
        LocalDate twoDaysBefore = date.minusDays(2);
        LocalDate oneDayBefore = date.minusDays(1);
        LocalDate oneDayAfter = date.plusDays(1);
        LocalDate twoDaysAfter = date.plusDays(2);

        Shift twoDaysBeforeShift = context.getFinalSchedule().getOrDefault(twoDaysBefore, new HashMap<>()).getOrDefault(empl, context.getDefaultDaysOffShift());
        Shift oneDayBeforeShift = context.getFinalSchedule().getOrDefault(oneDayBefore, new HashMap<>()).getOrDefault(empl, context.getDefaultDaysOffShift());
        Shift oneDayAfterShift = context.getFinalSchedule().getOrDefault(oneDayAfter, new HashMap<>()).getOrDefault(empl, context.getDefaultDaysOffShift());
        Shift twoDaysAfterShift = context.getFinalSchedule().getOrDefault(twoDaysAfter, new HashMap<>()).getOrDefault(empl, context.getDefaultDaysOffShift());

        return getShiftLength(twoDaysBeforeShift) + getShiftLength(oneDayBeforeShift) + getShiftLength(oneDayAfterShift) + getShiftLength(twoDaysAfterShift);
    }

    private Comparator<Employee> employeeWithLowestWorkedAroundDays(ScheduleGeneratorContext context, LocalDate date) {
        return Comparator.comparingInt(
                empl -> {
                    return calculateHoursCountTwoDaysBeforeAndTwoDaysAfter(context, date, empl);
                });
    }


    private void applyOpenStoreEmployee(ScheduleGeneratorContext context, LocalDate date, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        log.info("");
        log.info("applyOpenStoreEmployee");

        Optional<Employee> employeeToOpenStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .sorted(sortByWorkedHoursAndSpecialSortForWeekends(context, date))
                .findFirst();

        if (employeeToOpenStore.isEmpty()){
            log.info("Brak dostępnego pracownika, który może otworzyć sklep w dniu {}", date);

            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                    "Brak pracownika mogącego otworzyć sklep w dniu: " + date,
                    null,
                    date)
            );
            return;
        }

        Optional<Shift> openShift = shiftsSorted.stream().min(longestOpenStoreShift());

        if (openShift.isEmpty()){
            log.info("Brak dostępnej zmiany otwierającej sklep w dniu {}", date);
            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                    "Brak dostępnej otwierącej zmiany w dniu: " + date,
                    null,
                    date
                    )
            );
            return;
        }

        whenEmployeeHoursExceeded(context, date, employeeToOpenStore.get());
        whenEmployeeWorkingDaysExceeded(context, date, employeeToOpenStore.get());

        context.registerShiftOnSchedule(date,employeeToOpenStore.get(),openShift.get(),date.getDayOfWeek());
        shiftsSorted.remove(openShift.get());
        availableEmployees.remove(employeeToOpenStore.get());
        log.info("");
    }

    private Comparator<Employee> sortByWorkedHoursAndSpecialSortForWeekends(ScheduleGeneratorContext context, LocalDate date) {
        return (e1, e2) -> {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return Comparator.<Employee>comparingInt(
                                emp -> context.getWorkingOnWeekendCount().getOrDefault(emp, 0))
                        .thenComparingInt(emp -> context.getWorkingDaysCount().getOrDefault(emp, 0))
                        .compare(e1, e2);
            } else {
                return employeeWithLowestHours(context,date).compare(e1, e2);
            }
        };
    }

    private static Comparator<Shift> longestCloseStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getEndHour().getHour()
                ).reversed()
                .thenComparing(
                        Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ).reversed()
                );
    }

    private static Comparator<Shift> longestOpenStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getStartHour().getHour()
                )
                .thenComparing(
                        Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ).reversed()
                );
    }

    private void applyCashierIfPresent(ScheduleGeneratorContext context, LocalDate date, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Iterator<Employee> iterator = availableEmployees.iterator();
        while (iterator.hasNext()) {
            Employee employee = iterator.next();

            if (employee.isCashier()){
                if (whenEmployeeHoursExceeded(context,date,employee)) break;
                if (whenEmployeeWorkingDaysExceeded(context,date,employee)) break;

                Optional<Shift> longestCloseShift = shiftsSorted.stream().min(longestCloseStoreShift());

                if (longestCloseShift.isEmpty()){
                    context.registerMessageOnSchedule(
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.WARNING,
                                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                                    "Brak dostępnej zamykającej zmiany w dniu: " + date,
                                    null,
                                    date
                            )
                    );
                    return;
                }

                Shift chosenShift = longestCloseShift.get();

                context.registerShiftOnSchedule(date,employee,chosenShift,date.getDayOfWeek());
                shiftsSorted.remove(chosenShift);
                iterator.remove();
            }
        }
    }

    private boolean whenEmployeeWorkingDaysExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
        if (context.getWorkingDaysCount().getOrDefault(employee,0) >= calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth())) {
            log.info("Miesięczna suma przepracowanych dni u {} {} przekroczyła maksymalną ilość w dniu {}", employee.getFirstName(), employee.getLastName(), day);

            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.ERROR,
                    ScheduleMessageCode.EMPLOYEE_MONTHLY_MAX_WORKING_DAYS_EXCEEDED,
                    "Miesięczna suma przepracowanych dni u " + employee.getFirstName() + " " + employee.getLastName() + " przekroczyła maksymalną ilość w dniu " + day,
                    employee.getId(),
                    day)
            );

            return true;
        }
        return false;
    }

    private boolean whenEmployeeHoursExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
        if (context.getEmployeeHours().getOrDefault(employee,0) >= calendarCalculation.getMonthlyStandardWorkingHours(context.getYear(), context.getMonth())) {
            log.info("Miesięczna suma przepracowanych godzin u {} {} została przekroczona w dniu {}", employee.getFirstName(),employee.getLastName(),day);

            return true;
        }
        return false;
    }

    private int getShiftLength(Shift shift){
        return shift.getEndHour().getHour() - shift.getStartHour().getHour();
    }
}
