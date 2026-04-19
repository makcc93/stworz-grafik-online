package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.AnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProposalShiftApplier {
    private final HolidayManager holidayManager;
    private final ScheduleAnalyzer scheduleAnalyzer;

    public void applyProposalShiftsToSchedule(ScheduleGeneratorContext context){
        log.info("Sprawdzam propozycje zmian do dodania do grafika");

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
                    if (employeeIsOnVacation(context, employee, day, date)) continue;
                    if (employeeIsOnDayOff(context, employee, day, date)) continue;

                    scheduleAnalyzer.analyzeAndResolve(context,date, Collections.emptyList(),Collections.emptyList(), AnalyzeType.TOO_MANY_SHIFT_PROPOSALS);

                    int[] proposalShiftAsArray = context.employeeProposalShiftAsArray(employee, date);
                    Shift proposalShift = context.findShiftByArray(proposalShiftAsArray);

                    log.info("Wprowadzam propozycję zmiany {}-{} pracownika {} {} w dniu {}",
                            proposalShift.getStartHour().getHour(),
                            proposalShift.getEndHour().getHour(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            date);

                    context.registerShiftOnSchedule(date,employee,proposalShift,date.getDayOfWeek());
//                    context.addWorkingInformation(employee,proposalShift,date.getDayOfWeek());
                }
            }
        }


    }

    private static boolean employeeIsOnDayOff(ScheduleGeneratorContext context, Employee employee, int day, LocalDate date) {
        if (context.employeeIsOnDayOff(employee, day)){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.INFO,
                            ScheduleMessageCode.EMPLOYEE_DOUBLE_PROPOSAL,
                            "Pracownik " + employee.getFirstName() + " " + employee.getLastName() + " ma propozycję dnia wolnego i pracy w dniu " + date,
                            employee.getId(),
                            date
                    )
            );

            return true;
        }
        return false;
    }

    private static boolean employeeIsOnVacation(ScheduleGeneratorContext context, Employee employee, int day, LocalDate date) {
        if (context.employeeIsOnVacation(employee, day)){
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.INFO,
                            ScheduleMessageCode.EMPLOYEE_DOUBLE_PROPOSAL,
                            "Pracownik " + employee.getFirstName() + " " + employee.getLastName() + " jest urlopie i ma propozycję pracy w dniu " + date,
                            employee.getId(),
                            date
                    )
            );

            return true;
        }
        return false;
    }
}
