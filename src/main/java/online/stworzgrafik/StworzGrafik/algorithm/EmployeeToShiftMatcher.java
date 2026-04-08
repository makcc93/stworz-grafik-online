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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;
    private final CalendarCalculation calendarCalculation;
    private final ScheduleAnalyzer scheduleAnalyzer;

    public void matchEmployeeToShift(ScheduleGeneratorContext context) {
        log.info("Dopasowuję zmiany do pracowników");

        Map<LocalDate, int[]> originalStoreDrafts = context.getUneditedOriginalDateStoreDraft();
        LinkedHashMap<LocalDate, int[]> everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc = context.getEveryDayStoreDemandDraftWorkingOn();

        Map<LocalDate, List<Shift>> generatedShiftsByDate = context.getGeneratedShiftsByDay();
        List<Employee> storeActiveEmployees = context.getStoreActiveEmployees();

        for (Map.Entry<LocalDate,int[]> entry : everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc.entrySet()) {
            LocalDate day = entry.getKey();

            int[] uneditedOriginalStoreDailyDraft = originalStoreDrafts.get(day);

            log.info("oryginalny draft w dniu {}      - {}", day, uneditedOriginalStoreDailyDraft);
            log.info("draft po propozycjach w dniu {} - {}", day, entry.getValue());

            log.info("ROBIE TEST LOGA W DNIU {}", day);

            if (dayIsHolidayOrHasEmptyDemandDraft(day, everyDayStoreDemandDraftAfterProposalsSortedByDraftDesc)) {
                log.info("Pomijam dzień {} ponieważ jest świętem lub brak ustalonego zapotrzebowania na pracę", day);

                continue;
            }

            List<Employee> availableEmployees = getAvailableEmployees(context, storeActiveEmployees, day);
            List<Shift> shiftsSorted = getShiftsSortedByStartHour(generatedShiftsByDate, day);

            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees,AnalyzeType.UNDERSTAFFED);
            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees, AnalyzeType.TOO_MANY_PROPOSALS);
            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees,AnalyzeType.MANAGER_OPENING_HOUR);
            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees,AnalyzeType.MANAGER_CLOSING_HOUR);

            if (!morningOpenStoreEmployeeAlreadyInProposal(context,day,uneditedOriginalStoreDailyDraft)) {
                applyOpenStoreEmployee(context, day, availableEmployees, shiftsSorted);
            }

            if (!afternoonCloseStoreEmployeeAlreadyInProposal(context,day,uneditedOriginalStoreDailyDraft)) {
                applyCloseStoreEmployee(context, day, availableEmployees, shiftsSorted);
            }

            applyCashierIfPresent(context, day, availableEmployees, shiftsSorted);

            if (!morningCreditEmployeeAlreadyInProposal(context,day,uneditedOriginalStoreDailyDraft)){
                applyMorningCreditEmployee(context, availableEmployees, shiftsSorted, day);
            }

            if (!afternoonCreditEmployeeAlreadyInProposal(context,day,uneditedOriginalStoreDailyDraft)){
                applyAfternoonCreditEmployee(context, availableEmployees, shiftsSorted, day);
            }

            while (!shiftsSorted.isEmpty()) {
                Optional<Shift> shift = shiftsSorted.stream().min(longestShift());

                if (shift.isEmpty()){
                    log.info("Brak dostępnych zmian do rozdysponowania w dniu {}", day);
                    context.registerMessageOnSchedule(
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.INFO,
                                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                                    "Brak dostępnych zmian w dniu: " + day,
                                    null,
                                    day)
                    );
                    break;
                }

                Optional<Employee> employee = availableEmployees.stream().min(employeeWithLowestHours(context));

                if (employee.isEmpty()){
                    log.info("Brak dostępnych pracowników w dniu {}", day);
                    context.registerMessageOnSchedule(
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.INFO,
                                    ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                                    "Brak dostępnych pracowników w dniu: " + day,
                                    null,
                                    day
                            )
                    );
                    break;
                }

                saveMessageIfEmployeeHoursExceeded(context,day,employee.get());
                saveMessageIfEmployeeWorkingDaysExceeded(context,day,employee.get());

                context.registerShiftOnSchedule(day,employee.get(),shift.get());
                shiftsSorted.remove(shift.get());
                availableEmployees.remove(employee.get());
                context.addWorkingInformation(employee.get(),shift.get(),day.getDayOfWeek());
            }
        }
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
                .filter(empl -> !context.employeeIsOnReplacementOnWarehouse(day, empl))
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

    private boolean afternoonCloseStoreEmployeeAlreadyInProposal(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>());
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeProposal = proposalEntry.getValue();

            for (int i = 23; i >= 0; i--){
                if (dailyDraft[i] > 0){
                    if (employee.isCanOpenCloseStore() && employeeProposal[i] > 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void applyAfternoonCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate day) {
        Optional<Employee> employeeToOperateAfternoonCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .min(employeeWithLowestHours(context));

        if (employeeToOperateAfternoonCredit.isEmpty()){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego sprzedaż ratalną popołudniu w dniu: " + day,
                            null,
                            day
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
                            "Brak dostępnej popołudniowej zmiany do zamknięcia w dniu: " + day,
                            null,
                            day
                    )
            );
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToOperateAfternoonCredit.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToOperateAfternoonCredit.get());

        context.registerShiftOnSchedule(day,employeeToOperateAfternoonCredit.get(),afternoonCreditShift.get());
        shiftsSorted.remove(afternoonCreditShift.get());
        availableEmployees.remove(employeeToOperateAfternoonCredit.get());
        context.addWorkingInformation(employeeToOperateAfternoonCredit.get(), afternoonCreditShift.get(), day.getDayOfWeek());
    }

    private void applyMorningCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate day) {
        Optional<Employee> employeeToOperateMorningCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit).min(employeeWithLowestHours(context));

        if (employeeToOperateMorningCredit.isEmpty()){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego sprzedaż ratalną rano w dniu: " + day,
                            null,
                            day
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
                            "Brak dostępnej otwierającej zmiany w dniu: " + day,
                            null,
                            day
                    )
            );
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToOperateMorningCredit.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToOperateMorningCredit.get());

        context.registerShiftOnSchedule(day,employeeToOperateMorningCredit.get(),morningCreditShift.get());
        shiftsSorted.remove(morningCreditShift.get());
        availableEmployees.remove(employeeToOperateMorningCredit.get());
        context.addWorkingInformation(employeeToOperateMorningCredit.get(), morningCreditShift.get(), day.getDayOfWeek());
    }

    private boolean afternoonCreditEmployeeAlreadyInProposal(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day, new HashMap<>());
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeProposal = proposalEntry.getValue();

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

    private boolean morningOpenStoreEmployeeAlreadyInProposal(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day,new HashMap<>());
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeProposal = proposalEntry.getValue();

            for (int i = 0; i < dailyDraft.length; i++){
                if (dailyDraft[i] > 0){
                    if (employee.isCanOpenCloseStore() && employeeProposal[i] > 0){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean morningCreditEmployeeAlreadyInProposal(ScheduleGeneratorContext context, LocalDate day, int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day,new HashMap<>());
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeProposal = proposalEntry.getValue();

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

    private void applyCloseStoreEmployee(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Optional<Employee> employeeToCloseStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore).min(employeeWithLowestHours(context));

        if (employeeToCloseStore.isEmpty()) {
            log.info("Brak dostępnego pracownika mogącego zamknąć sklep w dniu {}", day);

            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika mogącego zamknąć sklep w dniu: " + day,
                            null,
                            day
                    )
            );
        }

        Optional<Shift> closeShift = shiftsSorted.stream().min(longestCloseStoreShift());

        if (closeShift.isEmpty()){
            log.info("Brak dostępnej zmiany do zamknięcia sklepu w dniu {}", day);
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej zamykącej zmiany w dniu: " + day,
                            null,
                            day
                    )
            );
        }

        if (employeeToCloseStore.isEmpty() || closeShift.isEmpty()) {
            modifyOpenStoreEmployeeHoursToAllDayShift(context, day);
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToCloseStore.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToCloseStore.get());

        context.registerShiftOnSchedule(day,employeeToCloseStore.get(),closeShift.get());
        shiftsSorted.remove(closeShift.get());
        availableEmployees.remove(employeeToCloseStore.get());
        context.addWorkingInformation(employeeToCloseStore.get(), closeShift.get(), day.getDayOfWeek());
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
        context.updateEmployeeHours(employee,shift,newShiftFromOpenToClose);

        context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                ScheduleMessageType.INFO,
                ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                "Z powodu brak pracownika mogącego zamknąć sklep w dniu: " + date +
                        " zmieniony został grafik " + employee.getFirstName() + " "
                        + employee.getLastName() + " na całodniową zmianę od "
                        + newShiftFromOpenToClose.getStartHour().getHour() + " do "
                        + newShiftFromOpenToClose.getEndHour().getHour(),
                employee.getId(),
                date)
        );
    }

    private Shift getShiftFromOpenToCloseStoreHours(ScheduleGeneratorContext context, LocalDate date){
        OpenCloseStoreHoursDTO dto = context.getStoreOpenCloseHoursByDate(date);

        return context.findShiftByHours(LocalTime.of(dto.openHour(), 0), LocalTime.of(dto.closeHour(), 0));
    }

    private Comparator<Employee> employeeWithLowestHours(ScheduleGeneratorContext context) {
        return Comparator.comparingInt(
                empl -> context.getEmployeeHours().getOrDefault(empl,0));
    }

    private void applyOpenStoreEmployee(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Optional<Employee> employeeToOpenStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore).min(employeeWithLowestHours(context));

        if (employeeToOpenStore.isEmpty()){
            log.info("Brak dostępnego pracownika, który może otworzyć sklep w dniu {}", day);

            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                    "Brak pracownika mogącego otworzyć sklep w dniu: " + day,
                    null,
                    day)
            );
            return;
        }

        Optional<Shift> openShift = shiftsSorted.stream().min(longestOpenStoreShift());

        if (openShift.isEmpty()){
            log.info("Brak dostępnej zmiany otwierającej sklep w dniu {}", day);
            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                    "Brak dostępnej otwierącej zmiany w dniu: " + day,
                    null,
                    day
                    )
            );
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToOpenStore.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToOpenStore.get());

        context.registerShiftOnSchedule(day,employeeToOpenStore.get(),openShift.get());
        shiftsSorted.remove(openShift.get());
        availableEmployees.remove(employeeToOpenStore.get());
        context.addWorkingInformation(employeeToOpenStore.get(), openShift.get(), day.getDayOfWeek());

        log.info("Liczba godzin pracy {} {} to {}",
                employeeToOpenStore.get().getFirstName(),
                employeeToOpenStore.get().getLastName(),
                context.getEmployeeHours().get(employeeToOpenStore.get()));
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

    private void applyCashierIfPresent(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Iterator<Employee> iterator = availableEmployees.iterator();
        while (iterator.hasNext()) {
            Employee employee = iterator.next();
            if (employee.isCashier()) {
                Optional<Shift> longestCloseShift = shiftsSorted.stream().min(longestCloseStoreShift());

                if (longestCloseShift.isEmpty()){
                    context.registerMessageOnSchedule(
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.WARNING,
                                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                                    "Brak dostępnej zamykającej zmiany w dniu: " + day,
                                    null,
                                    day
                            )
                    );
                    return;
                }

                saveMessageIfEmployeeHoursExceeded(context,day,employee);
                saveMessageIfEmployeeWorkingDaysExceeded(context,day,employee);

                context.registerShiftOnSchedule(day,employee,longestCloseShift.get());
                shiftsSorted.remove(longestCloseShift.get());
                iterator.remove();
                context.addWorkingInformation(employee, longestCloseShift.get(), day.getDayOfWeek());
            }
        }
    }

    private void saveMessageIfEmployeeWorkingDaysExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
        if (context.getWorkingDaysCount().getOrDefault(employee,0) > calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth())){
            log.info("Miesięczna suma przepracowanych dni u {} {} przekroczyła maksymalną ilość w dniu {}",employee.getFirstName(),employee.getLastName(),day);

            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.ERROR,
                    ScheduleMessageCode.EMPLOYEE_MONTHLY_MAX_WORKING_DAYS_EXCEEDED,
                    "Miesięczna suma przepracowanych dni u " + employee.getFirstName() + " " + employee.getLastName() + " przekroczyła maksymalną ilość w dniu "  + day,
                    employee.getId(),
                    day)
            );
        }
    }

    private void saveMessageIfEmployeeHoursExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
        if (context.getEmployeeHours().getOrDefault(employee,0) > calendarCalculation.getMonthlyStandardWorkingHours(context.getYear(), context.getMonth())) {
            log.info("Miesięczna suma przepracowanych godzin u {} {} została przekroczona w dniu {}", employee.getFirstName(),employee.getLastName(),day);

            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.EMPLOYEE_MONTHLY_SUM_OF_HOURS_EXCEEDED,
                    "Miesięczna suma przepracowanych godzin u  "
                            + employee.getFirstName() + " "
                            + employee.getLastName() + " została przekroczona w dniu "
                            + day,
                    employee.getId(),
                    day)
            );
        }
    }
}
