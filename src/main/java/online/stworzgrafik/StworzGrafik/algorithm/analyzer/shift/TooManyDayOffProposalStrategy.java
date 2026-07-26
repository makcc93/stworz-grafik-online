package online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TooManyDayOffProposalStrategy implements ScheduleAnalysisStrategy{

    private final CalendarCalculation calendarCalculation;

    @Override
    public ShiftAnalyzeType getSupportedType() {
        return ShiftAnalyzeType.TOO_MANY_DAY_OFF_PROPOSALS;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> employees) {
        return new TooManyDayOffProposalResult(employees,shifts);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((TooManyDayOffProposalResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        log.info("SPRAWDZENIE ZBYT WIELU PROPOZYCJI DNI WOLNYCH");
        List<Employee> availableEmployees = ((TooManyDayOffProposalResult) result).availableEmployees();
        List<Shift> shifts = ((TooManyDayOffProposalResult) result).shifts();

        while (shifts.size() > availableEmployees.size()) {
            boolean resolved = cancelProposalDayOffAndAddEmployeeToAvailable(availableEmployees, context, day);

            if (!resolved) break;
        }
    }

    private boolean cancelProposalDayOffAndAddEmployeeToAvailable(List<Employee> availableEmployees, ScheduleGeneratorContext context, LocalDate day){
        List<Employee> candidatesWithDayOffProposal = getEmployeesWithDayOffProposal(context, day, availableEmployees);

        if (candidatesWithDayOffProposal.isEmpty()) {
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z propozycją dnia wolnego do anulowania",
                            null,
                            day
                    )
            );
            return false;
        }

        int monthlyMaxWorkingDays = calendarCalculation.getMonthlyMaxWorkingDays(context.getYear(), context.getMonth());

        Comparator<Employee> byBiggestBufferToLimits = Comparator
                .comparing((Employee empl) -> context.getRemainingHoursUntilLimit(empl))
                .reversed()
                .thenComparing(Comparator.<Employee>comparingInt(
                        empl -> monthlyMaxWorkingDays - context.getWorkingDaysCount().getOrDefault(empl, 0)
                ).reversed());

        List<Employee> withinLimits = candidatesWithDayOffProposal.stream()
                .filter(empl -> context.isEmployeeUnderHoursLimit(empl))
                .filter(empl -> context.getWorkingDaysCount().getOrDefault(empl, 0) < monthlyMaxWorkingDays)
                .sorted(byBiggestBufferToLimits)
                .toList();

        boolean limitAlreadyExceeded = withinLimits.isEmpty();

        Employee chosenEmployee = limitAlreadyExceeded
                ? candidatesWithDayOffProposal.stream().sorted(byBiggestBufferToLimits).findFirst().orElseThrow()
                : withinLimits.get(0);

        context.deleteShiftFromSchedule(day, chosenEmployee);
        context.deleteEmployeeDayOffProposal(day, chosenEmployee);
        availableEmployees.add(chosenEmployee);

        if (limitAlreadyExceeded) {
            registerLimitViolation(context, day, chosenEmployee, monthlyMaxWorkingDays);
        }

        log.info("Propozycja dnia wolnego dla {} {} na dzień {} została anulowana z powodu zbyt małej liczby dostępnych pracowników. Uzasadnienie: ten pracownik ma największy zapas do limitu godzin/dni pracy.",
                chosenEmployee.getFirstName(),
                chosenEmployee.getLastName(),
                day);

        context.registerMessageOnSchedule(
                new CreateScheduleMessageDTO(
                        ScheduleMessageType.INFO,
                        ScheduleMessageCode.UNDERSTAFFED,
                        "Propozycja dnia wolnego dla " +
                                chosenEmployee.getFirstName() +
                                " " +
                                chosenEmployee.getLastName() +
                                " na dzień " + day +
                                " została anulowana z powodu zbyt małej liczby dostępnych pracowników. Uzasadnienie: ten pracownik ma największy zapas do limitu godzin/dni pracy.",
                        chosenEmployee.getId(),
                        day
                )
        );

        return true;
    }

    private void registerLimitViolation(ScheduleGeneratorContext context, LocalDate day, Employee employee, int monthlyMaxWorkingDays) {
        boolean hoursExceeded = !context.isEmployeeUnderHoursLimit(employee);
        boolean daysExceeded = context.getWorkingDaysCount().getOrDefault(employee, 0) >= monthlyMaxWorkingDays;
        String exceededLimitDescription = hoursExceeded && daysExceeded ? "godzin i dni pracy" : hoursExceeded ? "godzin pracy" : "dni pracy";

        log.warn("Brak pracownika z propozycją dnia wolnego mieszczącego się w limicie godzin/dni pracy w dniu {} - anuluję dzień wolny dla {} {} mimo to (przekroczony limit: {}), żeby nie zostawić zmiany bez obsady",
                day, employee.getFirstName(), employee.getLastName(), exceededLimitDescription);

        context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                ScheduleMessageType.WARNING,
                daysExceeded ? ScheduleMessageCode.EMPLOYEE_MONTHLY_MAX_WORKING_DAYS_EXCEEDED : ScheduleMessageCode.EMPLOYEE_MONTHLY_SUM_OF_HOURS_EXCEEDED,
                "Pracownikowi " + employee.getFirstName() + " " + employee.getLastName() +
                        " anulowano dzień wolny w dniu " + day +
                        " mimo przekroczonego limitu " + exceededLimitDescription +
                        " - brak innego dostępnego pracownika z propozycją dnia wolnego mieszczącego się w limicie.",
                employee.getId(),
                day));
    }

    private static List<Employee> getEmployeesWithDayOffProposal(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees) {
        Map<Employee, int[]> monthlyEmployeesProposalDayOff = context.getMonthlyEmployeesProposalDayOff();
        List<Employee> candidates = new ArrayList<>();

        for (Map.Entry<Employee, int[]> entry : monthlyEmployeesProposalDayOff.entrySet()) {
            Employee employee = entry.getKey();
            int[] monthlyProposal = entry.getValue();

            if (availableEmployees.contains(employee) || monthlyProposal[day.getDayOfMonth() - 1] == 0 || employee.isWarehouseman()) continue;

            candidates.add(employee);
        }
        return candidates;
    }
}