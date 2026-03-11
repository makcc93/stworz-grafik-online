package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
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
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProposalShiftApplier {
    private final HolidayManager holidayManager;
    private final ScheduleDetailsService scheduleDetailsService;
    private final ScheduleMessageService scheduleMessageService;
    private final ShiftEntityService shiftEntityService;

    public void applyProposalShiftsToSchedule(ScheduleGeneratorContext context){
        List<Employee> employees = context.getStoreActiveEmployees();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date)) {
                continue;
            }

            for (Employee employee : employees){
                if (context.employeeHasProposalShift(employee,date)){
                    if (context.employeeIsOnVacation(employee,day)){
                        scheduleMessageService.addMessage(
                                context.getSchedule().getId(),
                                new CreateScheduleMessageDTO(
                                        ScheduleMessageType.INFO,
                                        ScheduleMessageCode.EMPLOYEE_DOUBLE_PROPOSAL,
                                        "Employee " + employee.getFirstName() + " " + employee.getLastName() + " is on vacation and has proposal shift on day " + date,
                                        employee.getId(),
                                        date
                                )
                        );
                        continue;
                    }

                    if (context.employeeIsOnDayOff(employee,day)){
                        scheduleMessageService.addMessage(
                                context.getSchedule().getId(),
                                new CreateScheduleMessageDTO(
                                        ScheduleMessageType.INFO,
                                        ScheduleMessageCode.EMPLOYEE_DOUBLE_PROPOSAL,
                                        "Employee " + employee.getFirstName() + " " + employee.getLastName() + " has proposal day off and concrete shift in same time on day " + date,
                                        employee.getId(),
                                        date
                                )
                        );
                        continue;
                    }

                    int[] proposalShiftAsArray = context.employeeProposalShiftAsArray(employee, date);
                    Shift proposalShift = shiftEntityService.getArrayAsShift(proposalShiftAsArray);

                    registerProposalShiftOnSchedule(context,employee,date,proposalShift);
                    context.addWorkingInformation(employee,proposalShift,date.getDayOfWeek());
                }
            }

        }
    }

    private void registerProposalShiftOnSchedule(ScheduleGeneratorContext context, Employee employee, LocalDate date, Shift shift) {
        scheduleDetailsService.addScheduleDetails(
                context.getStoreId(),
                context.getSchedule().getId(),
                new CreateScheduleDetailsDTO(
                        employee.getId(),
                        date,
                        shift.getId(),
                        context.getProposalShiftTypeConfig().getId()
                )
        );
    }
}
