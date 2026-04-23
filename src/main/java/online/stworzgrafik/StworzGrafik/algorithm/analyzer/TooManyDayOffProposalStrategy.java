package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TooManyDayOffProposalStrategy implements ScheduleAnalysisStrategy{

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.TOO_MANY_DAY_OFF_PROPOSALS;
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
        List<Employee> availableEmployees = ((TooManyDayOffProposalResult) result).availableEmployees();
        List<Shift> shifts = ((TooManyDayOffProposalResult) result).shifts();

        while (shifts.size() > availableEmployees.size()) {
            boolean resolved = cancelProposalDayOffAndAddEmployeeToAvailable(availableEmployees, context, day);

            if (!resolved) break;
        }
    }

    private boolean cancelProposalDayOffAndAddEmployeeToAvailable(List<Employee> availableEmployees, ScheduleGeneratorContext context, LocalDate day){
        Map<Employee, Integer> employeeProposalDayOffCount = getEmployeeProposalDayOffCount(context, day, availableEmployees);

        LinkedHashMap<Employee, Integer> sortedByProposalsCountDesc = employeeProposalDayOffCount.entrySet().stream()
                .sorted((key1, key2) -> key2.getValue().compareTo(key1.getValue()))
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (keyValue1, keyValue2) -> keyValue1,
                                LinkedHashMap::new
                        )
                );

        Optional<Employee> employeeWithHighestProposalsCount = sortedByProposalsCountDesc.keySet().stream().findFirst();
        if (employeeWithHighestProposalsCount.isEmpty()) {
            context.registerMessageOnSchedule(
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z największą liczbą propozycji dni wolnych",
                            null,
                            day
                    )
            );
            return false;
        }


        Employee chosenEmployee = employeeWithHighestProposalsCount.get();

        context.deleteShiftFromSchedule(day, chosenEmployee);
        context.deleteEmployeeDayOffProposal(day, chosenEmployee);
        availableEmployees.add(chosenEmployee);

        log.info("Propozycja dnia wolnego dla {} {} na dzień {} została anulowana z powodu zbyt małej liczby dostępnych pracowników. Uzasadnienie: ten pracownik ma najwięcej propozycji dni wolnych.",
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
                                " została anulowana z powodu zbyt małej liczby dostępnych pracowników. Uzasadnienie: ten pracownik ma najwięcej propozycji dni wolnych.",
                        chosenEmployee.getId(),
                        day
                )
        );

        log.info("");
        return true;
    }

    private static Map<Employee, Integer> getEmployeeProposalDayOffCount(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees) {
        Map<Employee, int[]> monthlyEmployeesProposalDayOff = context.getMonthlyEmployeesProposalDayOff();
        Map<Employee, Integer> employeeProposalDayOffCount = new HashMap<>();

        for (Map.Entry<Employee, int[]> entry : monthlyEmployeesProposalDayOff.entrySet()) {
            Employee employee = entry.getKey();
            int[] monthlyProposal = entry.getValue();

            if (availableEmployees.contains(employee) || monthlyProposal[day.getDayOfMonth() - 1] == 0 || employee.isWarehouseman()) continue;

            int proposalsCount = 0;
            for (int dayValue : monthlyProposal) {
                proposalsCount += dayValue;
            }

            employeeProposalDayOffCount.put(employee, proposalsCount);
        }
        return employeeProposalDayOffCount;
    }
}
