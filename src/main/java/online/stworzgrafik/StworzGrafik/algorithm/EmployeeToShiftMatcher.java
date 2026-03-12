package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ScheduleMessageService scheduleMessageService;

    public void matchEmployeeToShift(ScheduleGeneratorContext context) {
        Map<LocalDate, int[]> everyDayStoreDemandDraftSorted = context.getEveryDayStoreDemandDraftSorted();

        Map<LocalDate, List<Shift>> generatedShiftsByDate = context.getGeneratedShiftsByDate();


        for (LocalDate day : everyDayStoreDemandDraftSorted.keySet()) {
            if (holidayManager.isHoliday(day) || Arrays.stream(everyDayStoreDemandDraftSorted.getOrDefault(day, new int[24])).sum() == 0) {
                continue;
            }

            List<Employee> availableEmployees = new ArrayList<>(context.getStoreActiveEmployees().stream()
                    .filter(empl -> !context.employeeIsOnDayOff(empl, day.getDayOfMonth()))
                    .filter(empl -> !context.employeeIsOnVacation(empl, day.getDayOfMonth()))
                    .filter(empl -> !context.employeeHasProposalShift(empl, day))
                    .filter(empl -> !empl.isWarehouseman())
                    .filter(empl -> !context.employeeIsOnReplacementOnWarehouse(day,empl))
                    .toList()
            );

            List<Shift> shiftsSorted = new ArrayList<>(generatedShiftsByDate.getOrDefault(day, Collections.emptyList()).stream()
                    .sorted(Comparator.comparingInt(
                            shift -> shift.getStartHour().getHour()
                    ))
                    .toList()
            );

            while (!shiftsSorted.isEmpty()) {

                //** IF NUMBER OF SHIFTS IS BIGGER THAN NUMBER OF EMPLOYEES SAVE ERROR MESSAGE TO SCHEDULE
                if (availableEmployees.size() < shiftsSorted.size()) {
                    scheduleMessageService.addMessage(
                            context.getSchedule().getId(),
                            new CreateScheduleMessageDTO(
                                    ScheduleMessageType.ERROR,
                                    ScheduleMessageCode.DEMAND_DRAFT_NOT_COVERED,
                                    "Too few employees to cover store demand draft on day " + day,
                                    null,
                                    day
                            )
                    );
                }
                //**

                //**EMPLOYEE WHO CAN OPEN STORE
                Employee employeeToOpenStore = availableEmployees.stream()
                        .filter(Employee::isCanOpenCloseStore)
                        .sorted(Comparator.comparingInt(
                                empl -> context.getEmployeeHours().get(empl)
                        ))
                        .toList()
                        .getFirst();

                Shift openShift = shiftsSorted.getFirst();

                registerShiftToSchedule(context, employeeToOpenStore, day, openShift);
                shiftsSorted.remove(openShift);
                availableEmployees.remove(employeeToOpenStore);
                context.addWorkingInformation(employeeToOpenStore, openShift, day.getDayOfWeek());
                //**

                //**EMPLOYEE WHO CAN CLOSE STORE
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

                registerShiftToSchedule(context, employeeToCloseStore, day, closeShift);
                shiftsSorted.remove(closeShift);
                availableEmployees.remove(employeeToCloseStore);
                context.addWorkingInformation(employeeToCloseStore, closeShift, day.getDayOfWeek());
                //**

                //**CASHIER IF AVAILABLE
                for (Employee employee : availableEmployees) {
                    if (employee.isCashier()) {
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
                        availableEmployees.remove(employee);
                        context.addWorkingInformation(employee, longestEndingShift, day.getDayOfWeek());
                    }
                }
                //**

            }
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
