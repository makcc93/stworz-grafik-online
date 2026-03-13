package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
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

@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ScheduleMessageService scheduleMessageService;
    private final CalendarCalculation calendarCalculation;
    private final ShiftEntityService shiftEntityService;

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

            //** IF NUMBER OF SHIFTS IS BIGGER THAN NUMBER OF EMPLOYEES SAVE ERROR MESSAGE TO SCHEDULE
            employeesCountIsLessThanShiftCount(context, day, availableEmployees, shiftsSorted);
            //**

            while (!shiftsSorted.isEmpty()) {
                //TODO Z TABLICY KORKOWEJ CZYLI SPRAWDZENIE CZY PROPOSAL NIE BLOKUJE KIEROWNIKA DO OTWARCIA SKLEPU NP 3X 8-14 I NIE MA ZMIANY NA OTWARCIE DLA KIERO

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
        Employee employeeToOperateAfternoonCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .sorted(Comparator.comparingInt(
                        empl -> context.getEmployeeHours().get(empl)
                ))
                .toList()
                .getFirst();

        Shift afternoonCreditShift = shiftsSorted.stream()
                .sorted(Comparator.comparingInt(
                                (Shift shift) -> shift.getEndHour().getHour()
                        )
                )
                .toList()
                .getFirst();

        sumOfWorkingHoursExceeded(context, day, employeeToOperateAfternoonCredit);

        if (!sumOfWorkingDaysExceeded(context, day, employeeToOperateAfternoonCredit)) {
            registerShiftToSchedule(context, employeeToOperateAfternoonCredit, day, afternoonCreditShift);
            shiftsSorted.remove(afternoonCreditShift);
            availableEmployees.remove(employeeToOperateAfternoonCredit);
            context.addWorkingInformation(employeeToOperateAfternoonCredit, afternoonCreditShift, day.getDayOfWeek());
        }
    }

    private void applyMorningCreditEmployee(ScheduleGeneratorContext context, List<Employee> availableEmployees, List<Shift> shiftsSorted, LocalDate day) {
        Employee employeeToOperateMorningCredit = availableEmployees.stream()
                .filter(Employee::isCanOperateCredit)
                .sorted(Comparator.comparingInt(
                        empl -> context.getEmployeeHours().get(empl)
                ))
                .toList()
                .getFirst();

        Shift morningCreditShift = shiftsSorted.stream()
                .sorted(Comparator.comparingInt(
                                (Shift shift) -> shift.getStartHour().getHour()
                        )
                        .thenComparingInt(
                                shift -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                        ))
                .toList()
                .getFirst();

        sumOfWorkingHoursExceeded(context, day, employeeToOperateMorningCredit);

        if (!sumOfWorkingDaysExceeded(context, day, employeeToOperateMorningCredit)) {
            registerShiftToSchedule(context, employeeToOperateMorningCredit, day, morningCreditShift);
            shiftsSorted.remove(morningCreditShift);
            availableEmployees.remove(employeeToOperateMorningCredit);
            context.addWorkingInformation(employeeToOperateMorningCredit, morningCreditShift, day.getDayOfWeek());
        }
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
        Employee employeeToCloseStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .sorted(Comparator.comparingInt(
                        empl -> context.getEmployeeHours().get(empl)
                ))
                .toList()
                .getFirst();

        Shift closeShift = shiftsSorted.stream()
                .sorted(Comparator.comparingInt(
                        shift -> shift.getEndHour().getHour()
                ))
                .toList()
                .getFirst();

        sumOfWorkingHoursExceeded(context, day, employeeToCloseStore);

        if (!sumOfWorkingDaysExceeded(context, day, employeeToCloseStore)) {

            registerShiftToSchedule(context, employeeToCloseStore, day, closeShift);
            shiftsSorted.remove(closeShift);
            availableEmployees.remove(employeeToCloseStore);
            context.addWorkingInformation(employeeToCloseStore, closeShift, day.getDayOfWeek());
        }
    }

    private void applyOpenStoreEmployee(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Employee employeeToOpenStore = availableEmployees.stream()
                .filter(Employee::isCanOpenCloseStore)
                .sorted(Comparator.comparingInt(
                        empl -> context.getEmployeeHours().get(empl)
                ))
                .toList()
                .getFirst();

        Shift openShift = shiftsSorted.getFirst();

        sumOfWorkingHoursExceeded(context, day, employeeToOpenStore);

        if (!sumOfWorkingDaysExceeded(context, day, employeeToOpenStore)) {
            registerShiftToSchedule(context, employeeToOpenStore, day, openShift);
            shiftsSorted.remove(openShift);
            availableEmployees.remove(employeeToOpenStore);
            context.addWorkingInformation(employeeToOpenStore, openShift, day.getDayOfWeek());
        }
    }

    private void employeesCountIsLessThanShiftCount(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        if (availableEmployees.size() < shiftsSorted.size()) {
            scheduleMessageService.addMessage(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.DEMAND_DRAFT_NOT_COVERED,
                            "Too few employees to cover store demand draft on day " + day,
                            null,
                            day
                    )
            );
        }
    }

    private void applyCashierIfPresent(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees, List<Shift> shiftsSorted) {
        Iterator<Employee> iterator = availableEmployees.iterator();
        while (iterator.hasNext()) {
            Employee employee = iterator.next();
            if (employee.isCashier()) {
                sumOfWorkingHoursExceeded(context, day, employee);

                if (sumOfWorkingDaysExceeded(context, day, employee)) break;


                Shift longestEndingShift = shiftsSorted.stream()
                        .max(Comparator.comparingInt(
                                        (Shift shift) -> shift.getEndHour().getHour()
                                )
                                .thenComparingInt(
                                        (Shift shift) -> shift.getEndHour().getHour() - shift.getStartHour().getHour()
                                ))
                        .orElseThrow();

                registerShiftToSchedule(context, employee, day, longestEndingShift);
                shiftsSorted.remove(longestEndingShift);
                iterator.remove();
                context.addWorkingInformation(employee, longestEndingShift, day.getDayOfWeek());
            }
        }
    }

    private boolean sumOfWorkingDaysExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
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

            return true;
        } return false;
    }

    private void sumOfWorkingHoursExceeded(ScheduleGeneratorContext context, LocalDate day, Employee employee) {
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
