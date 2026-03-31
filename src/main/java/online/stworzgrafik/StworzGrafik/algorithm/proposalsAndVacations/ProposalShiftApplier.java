package online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.AnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.ScheduleAnalyzer;
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
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProposalShiftApplier {
    private final HolidayManager holidayManager;
    private final ScheduleAnalyzer scheduleAnalyzer;

    public void applyProposalShiftsToSchedule(ScheduleGeneratorContext context){
        List<Employee> employees = context.getStoreActiveEmployees();

        Integer year = context.getYear();
        Integer month = context.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        Map<Employee, int[]> employeeDailyProposal = new HashMap<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(year, month, day);

            if (holidayManager.isHoliday(date)) {
                continue;
            }

            for (Employee employee : employees){
                if (context.employeeHasProposalShift(employee,date)){
                    if (context.employeeIsOnVacation(employee,day)){
                        context.registerMessageOnSchedule(
                                new CreateScheduleMessageDTO(
                                        ScheduleMessageType.INFO,
                                        ScheduleMessageCode.EMPLOYEE_DOUBLE_PROPOSAL,
                                        "Pracownik " + employee.getFirstName() + " " + employee.getLastName() + " jest urlopie i ma propozycję pracy w dniu " + date,
                                        employee.getId(),
                                        date
                                )
                        );

                        continue;
                    }

                    if (context.employeeIsOnDayOff(employee,day)){
                        context.registerMessageOnSchedule(
                                new CreateScheduleMessageDTO(
                                        ScheduleMessageType.INFO,
                                        ScheduleMessageCode.EMPLOYEE_DOUBLE_PROPOSAL,
                                        "Pracownik " + employee.getFirstName() + " " + employee.getLastName() + " ma propozycję dnia wolnego i pracy w dniu " + date,
                                        employee.getId(),
                                        date
                                )
                        );

                        continue;
                    }

                    scheduleAnalyzer.analyzeAndResolve(context,date, Collections.emptyList(),Collections.emptyList(), AnalyzeType.TOO_MANY_PROPOSALS);

                    int[] proposalShiftAsArray = context.employeeProposalShiftAsArray(employee, date);
                    Shift proposalShift = context.findShiftByArray(proposalShiftAsArray);

                    log.info("Wprowadzam propozycję zmiany {}-{} pracownika {} {} w dniu {}",
                            proposalShift.getStartHour().getHour(),
                            proposalShift.getEndHour().getHour(),
                            employee.getFirstName(),
                            employee.getLastName(),
                            date);

                    context.registerShiftOnSchedule(date,employee,proposalShift);
                    context.addWorkingInformation(employee,proposalShift,date.getDayOfWeek());
                }
            }

        }
    }
}
