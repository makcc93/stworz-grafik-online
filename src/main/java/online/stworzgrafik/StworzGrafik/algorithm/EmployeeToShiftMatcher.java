package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.AnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final ScheduleMessageService scheduleMessageService;
    private final CalendarCalculation calendarCalculation;
    private final ShiftEntityService shiftEntityService;
    private final ScheduleAnalyzer scheduleAnalyzer;

    public void matchEmployeeToShift(ScheduleGeneratorContext context) {
        Map<LocalDate, int[]> originalStoreDrafts = context.getUneditedOriginalDateStoreDraft();
        Map<LocalDate, int[]> everyDayStoreDemandDraftAfterProposals = context.getEveryDayStoreDemandDraftWorkingOn();

        Map<LocalDate, List<Shift>> generatedShiftsByDate = context.getGeneratedShiftsByDay();


        for (Map.Entry<LocalDate,int[]> entry : everyDayStoreDemandDraftAfterProposals.entrySet()) {
            LocalDate day = entry.getKey();
            int[]uneditedOriginalStoreDailyDraft = originalStoreDrafts.get(day);

            if (holidayManager.isHoliday(day) || Arrays.stream(everyDayStoreDemandDraftAfterProposals.getOrDefault(day, new int[24])).sum() == 0) {
                continue;
            }

            List<Employee> availableEmployees = new ArrayList<>(context.getStoreActiveEmployees().stream()
                    .filter(empl -> !context.employeeIsOnDayOff(empl, day.getDayOfMonth()))
                    .filter(empl -> !context.employeeIsOnVacation(empl, day.getDayOfMonth()))
                    .filter(empl -> !context.employeeHasProposalShift(empl, day))
                    .filter(empl -> !empl.isWarehouseman())
                    .filter(empl -> !context.employeeIsOnReplacementOnWarehouse(day,empl))
                    .filter(empl ->
                            calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(),context.getMonth()) > context.getEmployeeHours().get(empl))
                    .toList()
            );

            List<Shift> shiftsSorted = new ArrayList<>(generatedShiftsByDate.getOrDefault(day, Collections.emptyList()).stream()
                    .sorted(Comparator.comparingInt(
                            shift -> shift.getStartHour().getHour()
                    ))
                    .toList()
            );

            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees,AnalyzeType.TOO_MANY_PROPOSALS);
            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees,AnalyzeType.OPENING_HOUR);
            scheduleAnalyzer.analyzeAndResolve(context,day,shiftsSorted,availableEmployees,AnalyzeType.CLOSING_HOUR);

            while (!shiftsSorted.isEmpty()) {

                //**CHECK OPEN STORE IN SCHEDULE
                if (!morningOpenStoreEmployeeAlreadyInSchedule(context,day,uneditedOriginalStoreDailyDraft)) {
                    //**EMPLOYEE WHO CAN OPEN STORE
                    applyOpenStoreEmployee(context, day, availableEmployees, shiftsSorted);
                }
                //**

                //**CHECK CLOSE STORE IN SCHEDULE
                if (!afternoonCloseStoreEmployeeAlreadyInSchedule(context,day,uneditedOriginalStoreDailyDraft)) {
                    //**EMPLOYEE WHO CAN CLOSE STORE
                    applyCloseStoreEmployee(context, day, availableEmployees, shiftsSorted);
                }
                //**

                //**CASHIER IF AVAILABLE
                applyCashierIfPresent(context, day, availableEmployees, shiftsSorted);
                //**

                //**CHECK MORNING PROPOSAL CREDIT EMPLOYEES
                if (!morningCreditEmployeeAlreadyInSchedule(context,day,uneditedOriginalStoreDailyDraft)){
                    //** EMPLOYEE TO OPERATE MORNING CREDIT
                    applyMorningCreditEmployee(context, availableEmployees, shiftsSorted, day);
                }
                //**

                //**CHECK AFTERNOON PROPOSAL CREDIT EMPLOYEE
                if (!afternoonCreditEmployeeAlreadyInSchedule(context,day,uneditedOriginalStoreDailyDraft)){
                    //** EMPLOYEE TO OPERATE AFTERNOON CREDIT
                    applyAfternoonCreditEmployee(context, availableEmployees, shiftsSorted, day);
                }
                //**
            }
        }
    }

    private boolean morningOpenStoreEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day,  int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeShift = proposalEntry.getValue();

            Shift arrayAsShift = shiftEntityService.getArrayAsShift(employeeShift);
            int startHour = arrayAsShift.getStartHour().getHour();
            if (employee.isCanOpenCloseStore() && dailyDraft[startHour] >= 1) {
                return true;
            }
        }
        return false;
    }

    private boolean afternoonCloseStoreEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day,  int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeShift = proposalEntry.getValue();

            Shift arrayAsShift = shiftEntityService.getArrayAsShift(employeeShift);
            int endHour = arrayAsShift.getEndHour().getHour();
            if (employee.isCanOpenCloseStore() && dailyDraft[endHour] == 0) {
                return true;
            }
        }
        return false;
    }

    private void applyAfternoonCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate day) {
        Optional<Employee> employeeToOperateAfternoonCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .sorted(employeeWithLowestHours(context))
                .findFirst();

        if (employeeToOperateAfternoonCredit.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego sprzedaż ratalną popołudniu w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        Optional<Shift> afternoonCreditShift = shiftsSorted.stream()
                .sorted(longestCloseStoreShift())
                .findFirst();

        if (afternoonCreditShift.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej popołudniowej zmiany do zamknięcia w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToOperateAfternoonCredit.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToOperateAfternoonCredit.get());

        registerShiftToSchedule(context, employeeToOperateAfternoonCredit.get(), day, afternoonCreditShift.get());
        shiftsSorted.remove(afternoonCreditShift.get());
        availableEmployees.remove(employeeToOperateAfternoonCredit.get());
        context.addWorkingInformation(employeeToOperateAfternoonCredit.get(), afternoonCreditShift.get(), day.getDayOfWeek());
    }

    private void applyMorningCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate day) {
        Optional<Employee> employeeToOperateMorningCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .sorted(employeeWithLowestHours(context))
                .findFirst();

        if (employeeToOperateMorningCredit.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika obsługującego sprzedaż ratalną rano w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        Optional<Shift> morningCreditShift = shiftsSorted.stream()
                .sorted(longestOpenStoreShift())
                        .findFirst();

        if (morningCreditShift.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej otwierającej zmiany w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToOperateMorningCredit.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToOperateMorningCredit.get());

        registerShiftToSchedule(context, employeeToOperateMorningCredit.get(), day, morningCreditShift.get());
        shiftsSorted.remove(morningCreditShift.get());
        availableEmployees.remove(employeeToOperateMorningCredit.get());
        context.addWorkingInformation(employeeToOperateMorningCredit.get(), morningCreditShift.get(), day.getDayOfWeek());

    }

    private boolean afternoonCreditEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day,  int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeShift = proposalEntry.getValue();

            Shift arrayAsShift = shiftEntityService.getArrayAsShift(employeeShift);
            int endHour = arrayAsShift.getEndHour().getHour();
            if (employee.isCanOperateCredit() && dailyDraft[endHour + 1] == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean morningCreditEmployeeAlreadyInSchedule(ScheduleGeneratorContext context, LocalDate day,  int[]dailyDraft) {
        Map<Employee, int[]> dailyProposal = context.getMonthlyEmployeesProposalShiftsByDate().get(day);
        for (Map.Entry<Employee, int[]> proposalEntry : dailyProposal.entrySet()) {
            Employee employee = proposalEntry.getKey();
            int[] employeeShift = proposalEntry.getValue();

            Shift arrayAsShift = shiftEntityService.getArrayAsShift(employeeShift);
            int startHour = arrayAsShift.getStartHour().getHour();
            if (employee.isCanOperateCredit() && dailyDraft[startHour] >= 1 || dailyDraft[startHour + 1] >= 1) {
                return true;
            }
        }
        return false;
    }

    private void applyCloseStoreEmployee(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Optional<Employee> employeeToCloseStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .sorted(employeeWithLowestHours(context))
                .findFirst();

        if (employeeToCloseStore.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika mogącego zamknąć sklep w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        Optional<Shift> closeShift = shiftsSorted.stream()
                .sorted(longestCloseStoreShift())
                .findFirst();

        if (closeShift.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej zamykącej zmiany w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToCloseStore.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToCloseStore.get());

        registerShiftToSchedule(context, employeeToCloseStore.get(), day, closeShift.get());
        shiftsSorted.remove(closeShift.get());
        availableEmployees.remove(employeeToCloseStore.get());
        context.addWorkingInformation(employeeToCloseStore.get(), closeShift.get(), day.getDayOfWeek());
    }

    private static Comparator<Employee> employeeWithLowestHours(ScheduleGeneratorContext context) {
        return Comparator.comparingInt(
                empl -> context.getEmployeeHours().get(empl)
        );
    }

    private void applyOpenStoreEmployee(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Optional<Employee> employeeToOpenStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .sorted(employeeWithLowestHours(context))
                .findFirst();

        if (employeeToOpenStore.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Brak pracownika mogącego otworzyć sklep w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        Optional<Shift> openShift = shiftsSorted.stream()
                .sorted(longestOpenStoreShift())
                .findFirst();

        if (openShift.isEmpty()){
            scheduleMessageService.addMessage(context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                            "Brak dostępnej otwierącej zmiany w dniu: " + day,
                            null,
                            day
                    ));
            return;
        }

        saveMessageIfEmployeeHoursExceeded(context, day, employeeToOpenStore.get());
        saveMessageIfEmployeeWorkingDaysExceeded(context, day, employeeToOpenStore.get());

        registerShiftToSchedule(context, employeeToOpenStore.get(), day, openShift.get());
        shiftsSorted.remove(openShift.get());
        availableEmployees.remove(employeeToOpenStore.get());
        context.addWorkingInformation(employeeToOpenStore.get(), openShift.get(), day.getDayOfWeek());
    }

    private static Comparator<Shift> longestCloseStoreShift() {
        return Comparator.comparingInt(
                        (Shift shift) -> shift.getEndHour().getHour()
                )
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
                saveMessageIfEmployeeHoursExceeded(context, day, employee);
                saveMessageIfEmployeeWorkingDaysExceeded(context, day, employee);

                Optional<Shift> longestCloseShift = shiftsSorted.stream()
                        .sorted(longestCloseStoreShift())
                        .findFirst();

                if (longestCloseShift.isEmpty()){
                    scheduleMessageService.addMessage(context.getSchedule().getId(),
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.WARNING,
                                    ScheduleMessageCode.NO_AVAILABLE_SHIFT,
                                    "Brak dostępnej zamykającej zmiany w dniu: " + day,
                                    null,
                                    day
                            ));
                    return;
                }

                registerShiftToSchedule(context, employee, day, longestCloseShift.get());
                shiftsSorted.remove(longestCloseShift.get());
                iterator.remove();
                context.addWorkingInformation(employee, longestCloseShift.get(), day.getDayOfWeek());
            }
        }
    }

    private void saveMessageIfEmployeeWorkingDaysExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
        if (context.getWorkingDaysCount().get(employee) > calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth())){
            scheduleMessageService.addMessage(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.ERROR,
                            ScheduleMessageCode.EMPLOYEE_MONTHLY_MAX_WORKING_DAYS_EXCEEDED,
                            "Employee " + employee.getFirstName() + " " + employee.getLastName() + " monthly working days has been exceeded, on date: " + day,
                            employee.getId(),
                            day
                    )
            );
        }
    }

    private void saveMessageIfEmployeeHoursExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
        if (context.getEmployeeHours().get(employee) > calendarCalculation.getMonthlyStandardWorkingHours(context.getYear(), context.getMonth())) {
            scheduleMessageService.addMessage(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.EMPLOYEE_MONTHLY_SUM_OF_HOURS_EXCEEDED,
                            "Employee " + employee.getFirstName() + " " + employee.getLastName() + " monthly sum of working hours has been exceeded, on date: " + day,
                            employee.getId(),
                            day
                    )
            );
        }
    }

    private void registerShiftToSchedule(ScheduleGeneratorContext context, Employee employee, LocalDate date, Shift shift){
        scheduleDetailsService.addScheduleDetails(
                context.getStoreId(),
                context.getSchedule().getId(),
                new CreateScheduleDetailsDTO(
                        employee.getId(),
                        date,
                        shift.getId(),
                        context.getStandardShiftTypeConfig().getId()
                )
        );
    }
}
