package online.stworzgrafik.StworzGrafik.algorithm;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.OpenCloseHoursForEmployeeIndexDTO;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ShiftAnalyzeType;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;
    private final CalendarCalculation calendarCalculation;
    private final ScheduleAnalyzer scheduleAnalyzer;

    public void matchEmployeeToShift(ScheduleGeneratorContext context) {
        log.info("DOPASOWANIE PRACOWNIKÓW DO ZMIAN");
        LinkedHashMap<LocalDate, int[]> everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc = context.getEveryDayStoreDemandDraftWorkingOn();

        Map<LocalDate, List<Shift>> generatedShiftsByDate = context.getGeneratedShiftsByDay();
        List<Employee> storeActiveEmployees = context.getStoreNotSpecialActiveEmployees();

        for (Map.Entry<LocalDate,int[]> entry : everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc.entrySet()) {
            LocalDate date = entry.getKey();

            if (dayIsHolidayOrHasEmptyDemandDraft(date, everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc)) {
                log.info("Pomijam dzień {} ponieważ jest świętem lub brak ustalonego zapotrzebowania na pracę", date);

                continue;
            }

            List<Employee> availableEmployees = getAvailableEmployees(context, storeActiveEmployees, date);
            List<Shift> shiftsSorted = getShiftsSortedByStartHour(generatedShiftsByDate, date);

            showShiftsInLog(shiftsSorted,date);

            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees, ShiftAnalyzeType.TOO_MANY_DAY_OFF_PROPOSALS);
            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees, ShiftAnalyzeType.TOO_MANY_SHIFT_PROPOSALS);
            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees, ShiftAnalyzeType.MANAGER_OPENING_HOUR);
            scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees, ShiftAnalyzeType.MANAGER_CLOSING_HOUR);

            if (!morningOpenStoreEmployeeAlreadyInSchedule(context,date)) {
                applyOpenStoreEmployee(context, date, availableEmployees, shiftsSorted);
            }

            if (!afternoonCloseStoreEmployeeAlreadyInSchedule(context,date)) {
                applyCloseStoreEmployee(context, date, availableEmployees, shiftsSorted);
            }

            applyCashierIfPresent(context, date, availableEmployees, shiftsSorted);

            if (!morningCreditEmployeeAlreadyInSchedule(context,date)){
                applyMorningCreditEmployee(context, availableEmployees, shiftsSorted, date);
            }

            if (!afternoonCreditEmployeeAlreadyInSchedule(context,date)){
                applyAfternoonCreditEmployee(context, availableEmployees, shiftsSorted, date);
            }

            if (!morningCheckoutEmployeeAlreadyInSchedule(context,date)){
                applyMorningCheckoutEmployee(context,availableEmployees,shiftsSorted,date);
            }

            if (!afternoonCheckoutEmployeeAlreadyInSchedule(context,date)){
                applyAfternoonCheckoutEmployee(context,availableEmployees,shiftsSorted,date);
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
                        .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));


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
                int sizeBefore = shiftsSorted.size();

                log.warn("Mamy więcej zmian niż pracowników w dniu {} - wdrażam działanie", date);
                scheduleAnalyzer.analyzeAndResolve(context,date,shiftsSorted,availableEmployees, ShiftAnalyzeType.UNDERSTAFFED);

                if (shiftsSorted.size() == sizeBefore) {
                    log.warn("Nie udało się rozwiązać niedoboru pracowników dla dnia: {} - pomijam", date);
                    break;
                }
            }

        }
    }

    private void showShiftsInLog(List<Shift> shifts, LocalDate date){
        String shiftsAsString = shifts.stream()
                .map(shift -> shift.getStartHour() + "-" + shift.getEndHour())
                .collect(Collectors.joining(" | "));

        log.info("Dzień: {} Zmiany: {}", date, shiftsAsString);
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
                .filter(empl -> !context.employeeHasProposalDaysOff(empl, day))
                .filter(empl -> !context.employeeIsOnVacation(empl, day))
                .filter(empl -> !context.employeeIsOnDelegation(empl, day))
                .filter(empl -> !context.employeeHasProposalShift(empl, day))
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !context.isEmployeeWorkingInWarehouse(empl,day))
                .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl,day))
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

    private boolean afternoonCloseStoreEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate date) {
        Map<Employee, Shift> employeeShift = context.getFinalSchedule().getOrDefault(date, new HashMap<>());
        for (Map.Entry<Employee, Shift> entry : employeeShift.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOpenCloseStore()) continue;

            int[] shiftAsArray = context.shiftAsArray(entry.getValue());
            int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();

            if (shiftAsArray[closeHour] > 0) return true;
        }

        return false;
    }

    private void applyAfternoonCheckoutEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees,List<Shift> shiftsSorted, LocalDate date){
        log.info("POPOŁUDNIU - KASA {}", date);
        Optional<Employee> employeeToOperateAfternoonCheckout = availableEmployees.stream()
                .filter(Employee::isCanOperateCheckout)
                .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));

        if (employeeToOperateAfternoonCheckout.isEmpty()){
            log.info("Brak pracownika");
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego kasę popołudniu w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Optional<Shift> afternoonCheckoutShift = shiftsSorted.stream().min(longestCloseStoreShift());

        if (afternoonCheckoutShift.isEmpty()){
            log.info("Brak zmiany");
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej popołudniowej zmiany w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Employee employeeToOperateCheckout = employeeToOperateAfternoonCheckout.get();
        Shift checkoutShift = afternoonCheckoutShift.get();

        whenEmployeeHoursExceeded(context, date, employeeToOperateCheckout);
        whenEmployeeWorkingDaysExceeded(context, date, employeeToOperateCheckout);

        context.registerShiftOnSchedule(date, employeeToOperateCheckout, checkoutShift,date.getDayOfWeek());

        shiftsSorted.remove(checkoutShift);
        availableEmployees.remove(employeeToOperateCheckout);
    }

    private void applyAfternoonCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate date) {
        Optional<Employee> employeeToOperateAfternoonCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));

        if (employeeToOperateAfternoonCredit.isEmpty()){
            log.info("Brak pracownika");
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
            log.info("Brak zmiany");
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej popołudniowej zmiany w dniu: " + date,
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

        shiftsSorted.remove(creditShift);
        availableEmployees.remove(employeeToOperateCredit);
    }

    private void applyMorningCheckoutEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate date){
        Optional<Employee> employeeToOperateMorningCheckout = availableEmployees.stream()
                .filter(Employee::isCanOperateCheckout)
                .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));

        if (employeeToOperateMorningCheckout.isEmpty()){
            log.info("Brak pracownika");
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego kasę rano w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Optional<Shift> morningCheckoutShift = shiftsSorted.stream().min(longestOpenStoreShift());

        if (morningCheckoutShift.isEmpty()){
            log.info("Brak zmiany");
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej zmiany w dniu: " + date,
                            null,
                            date
                    )
            );
            return;
        }

        Shift checkoutShift = morningCheckoutShift.get();
        Employee employeeToOperateCheckout = employeeToOperateMorningCheckout.get();

        whenEmployeeHoursExceeded(context, date, employeeToOperateCheckout);
        whenEmployeeWorkingDaysExceeded(context, date, employeeToOperateCheckout);

        context.registerShiftOnSchedule(date, employeeToOperateCheckout,checkoutShift,date.getDayOfWeek());

        shiftsSorted.remove(checkoutShift);
        availableEmployees.remove(employeeToOperateCheckout);
    }

    private void applyMorningCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate date) {
        Optional<Employee> employeeToOperateMorningCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));

        if (employeeToOperateMorningCredit.isEmpty()){
            log.info("Brak pracownika");
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
            log.info("Brak zmiany");
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

        shiftsSorted.remove(creditShift);
        availableEmployees.remove(employeeToOperateCredit);
    }

    private boolean afternoonCreditEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate date) {
        Map<Employee, Shift> map = context.getFinalSchedule().getOrDefault(date, new HashMap<>());

        for (Map.Entry<Employee, Shift> entry : map.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOperateCredit()) continue;

            int[] employeeShiftAsArray = context.shiftAsArray(entry.getValue());

            int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();
            int closeForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).closeHour();

            if (employeeShiftAsArray[closeHour] > 0 || employeeShiftAsArray[closeForClientsHour] > 0) return true;

        }
        return false;
    }

    private boolean morningOpenStoreEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate date) {
        Map<Employee, Shift> employeeShift = context.getFinalSchedule().getOrDefault(date,Map.of());
        for (Map.Entry<Employee, Shift> entry : employeeShift.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOpenCloseStore()) continue;

            int[] shiftAsArray = context.shiftAsArray(entry.getValue());
            int openHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).openHour();

            if (shiftAsArray[openHour] > 0) return true;

        }
        return false;
    }

    private boolean morningCheckoutEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate date){
        Map<Employee, Shift> map = context.getFinalSchedule().getOrDefault(date, Map.of());
        for (Map.Entry<Employee, Shift> entry : map.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOperateCheckout()) continue;

            int[] employeeProposal = context.shiftAsArray(entry.getValue());

            int openHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).openHour();
            int openForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).openHour();

            if(employeeProposal[openHour] > 0 || employeeProposal[openForClientsHour] > 0) return true;
        }
        return false;
    }

    private boolean afternoonCheckoutEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate date){
        Map<Employee, Shift> map = context.getFinalSchedule().getOrDefault(date, new HashMap<>());
        for (Map.Entry<Employee, Shift> entry : map.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOperateCheckout()) continue;

            int[] employeeProposal = context.shiftAsArray(entry.getValue());

            int closeHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).closeHour();
            int closeForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).closeHour();

            if(employeeProposal[closeHour] > 0 || employeeProposal[closeForClientsHour] > 0) return true;
        }
        return false;
    }

    private boolean morningCreditEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate date) {
        Map<Employee, Shift> map = context.getFinalSchedule().getOrDefault(date, new HashMap<>());
        for (Map.Entry<Employee, Shift> entry : map.entrySet()) {
            Employee employee = entry.getKey();
            if (!employee.isCanOperateCredit()) continue;

            int[] employeeShiftAsArray = context.shiftAsArray(entry.getValue());

            int openHour = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date).openHour();
            int openForClientsHour = context.getStoreOpenCloseHoursIndexForClientsByDate(date).openHour();

            if(employeeShiftAsArray[openHour] > 0 || employeeShiftAsArray[openForClientsHour] > 0) return true;
        }
        return false;
    }

    private void applyCloseStoreEmployee(ScheduleGeneratorContext context, LocalDate date, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Optional <Employee> employeeToCloseStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));

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
    }

    private void modifyOpenStoreEmployeeHoursToAllDayShift(ScheduleGeneratorContext context, LocalDate date){
        Employee employee = null;
        Shift shift = null;
        Map<Employee, Shift> dailySchedule = context.getFinalSchedule().getOrDefault(date, new HashMap<>());

        for (Employee e : dailySchedule.keySet()){
            if (e.isCanOpenCloseStore()
                    && context.employeeIsWorking(e,date)
                    && !context.employeeIsOnDelegation(e,date)
                    && !context.employeeIsOnVacation(e,date)
                    && !context.employeeHasProposalDaysOff(e,date)
                    && !context.isEmployeeOnRestRequirementDayOff(e,date)){
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
        OpenCloseHoursForEmployeeIndexDTO dto = context.getStoreOpenCloseHoursIndexForEmployeesByDate(date);

        return context.findShiftByHours(LocalTime.of(dto.openHour(), 0), LocalTime.of(dto.closeHour()+1, 0));
    }

    private Comparator<Employee> employeeWithLowestHours(ScheduleGeneratorContext context, LocalDate date) {
        return (Comparator.comparing(
                empl -> context.getEmployeeHours().getOrDefault(empl,BigDecimal.ZERO)));
    }

    private void applyOpenStoreEmployee(ScheduleGeneratorContext context, LocalDate date, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Optional<Employee> employeeToOpenStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .min(sortByWorkedHoursAndSpecialSortForWeekends(context, date));

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
    }

    private Comparator<Employee> sortByWorkedHoursAndSpecialSortForWeekends(ScheduleGeneratorContext context, LocalDate date) {
        Comparator<Employee> baseComparator;

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            baseComparator = Comparator.<Employee>comparingInt(
                            emp -> context.getWorkingOnWeekendCount().getOrDefault(emp, 0))
                    .thenComparingInt(emp -> - context.getVacationDaysCount().getOrDefault(emp,0))
                    .thenComparingInt(emp -> context.getWorkingDaysCount().getOrDefault(emp, 0))
                    .thenComparing(emp -> context.getEmployeeHours().getOrDefault(emp,BigDecimal.ZERO));
        } else {
            baseComparator = employeeWithLowestHours(context,date);
        }

        if (!context.isLastMonthOfPeriod()) {
            return baseComparator;
        }

        // W ostatnim miesiącu okresu rozliczeniowego liczy się trafienie w indywidualny,
        // potwierdzony limit/pozostałą liczbę godzin każdego pracownika (patrz
        // EmployeeMonthlyHoursConfirmation.confirmedHours ustawiane na froncie).
        //
        // POPRAWKA: baseComparator sortuje po SUROWYCH, dotychczas przepracowanych
        // godzinach (rosnąco) - to wyrównuje wszystkich pracowników do tego samego
        // poziomu godzin, zupełnie IGNORUJĄC że mają różne indywidualne limity.
        // Właśnie dlatego pracownik z potwierdzonym limitem 195h kończył grafik
        // z ok. 163h, a inny z limitem 175h - z 183h: algorytm traktował ich tak,
        // jakby cel był identyczny, zamiast dążyć do wypełnienia w każdym przypadku
        // WŁASNEGO limitu. Te same "surowe godziny" oznaczają zupełnie inny % realizacji
        // celu dla osoby z limitem 163h i dla osoby z limitem 195h.
        //
        // Dlatego priorytet przy wyborze pracownika do zmiany jest teraz następujący:
        // 1) kandydaci, którzy jeszcze mieszczą się w swoim limicie, przed tymi,
        //    którzy go już przekroczyli (bez zmian),
        // 2) spośród nich ten, komu zostało najwięcej godzin DO WYPRACOWANIA
        //    względem WŁASNEGO limitu (getRemainingHoursUntilLimit, malejąco) -
        //    to ta sama logika, która już działa poprawnie w
        //    HoursSwapperAnalysisStrategy i ShiftSwapperAnalysisStrategy (patrz
        //    komentarze "ZMIANA" w tamtych klasach) - teraz algorytm dopasowuje
        //    dobrze od razu, zamiast liczyć wyłącznie na korektę zamianami po fakcie,
        // 3) dopiero przy remisie (identyczny pozostały zapas godzin) - dotychczasowa
        //    reguła jako tie-breaker (uczciwość weekendowa / surowe godziny).
        Comparator<Employee> byRemainingHoursUntilLimitDesc =
                Comparator.comparing((Employee emp) -> context.getRemainingHoursUntilLimit(emp)).reversed();

        return Comparator.<Employee>comparingInt(emp -> context.isEmployeeUnderHoursLimit(emp) ? 0 : 1)
                .thenComparing(byRemainingHoursUntilLimitDesc)
                .thenComparing(baseComparator);
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
                if (whenEmployeeHoursExceeded(context,date,employee)) continue;
                if (whenEmployeeWorkingDaysExceeded(context,date,employee)) continue;

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
        if (!context.isEmployeeUnderHoursLimit(employee)) {
            log.info("Miesięczna suma przepracowanych godzin u {} {} została przekroczona w dniu {} i wynosi {} (limit: {})",
                    employee.getFirstName(),
                    employee.getLastName(),
                    day,
                    context.getEmployeeHours().getOrDefault(employee,BigDecimal.ZERO),
                    context.getEmployeeHoursLimit(employee));

            if (context.isLastMonthOfPeriod()) {
                context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                        ScheduleMessageType.WARNING,
                        ScheduleMessageCode.EMPLOYEE_MONTHLY_SUM_OF_HOURS_EXCEEDED,
                        "Brak dostępnego pracownika mieszczącego się w limicie godzin - " + employee.getFirstName() + " " + employee.getLastName() +
                                " przekroczył zatwierdzony limit godzin w dniu " + day,
                        employee.getId(),
                        day)
                );
            }

            return true;
        }
        return false;
    }
}