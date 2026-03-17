package online.stworzgrafik.StworzGrafik.algorithm.analyzer;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TooManyProposalsAnalysisStrategy implements ScheduleAnalysisStrategy{
    private final ScheduleMessageService scheduleMessageService;
    private final ShiftEntityService shiftEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final ScheduleDetailsService scheduleDetailsService;
    private final CalendarCalculation calendarCalculation;

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.TOO_MANY_PROPOSALS;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
               return new TooManyProposalsAnalysisResult(availableEmployees,shifts);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((TooManyProposalsAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        List<Employee> availableEmployees = ((TooManyProposalsAnalysisResult) result).availableEmployees();

        Map<Employee, int[]> monthlyEmployeesProposalDayOff = context.getMonthlyEmployeesProposalDayOff();
        Map<Employee, Integer> employeeProposalDayOffCount = new HashMap<>();

        for (Map.Entry<Employee, int[]> entry : monthlyEmployeesProposalDayOff.entrySet()){
            Employee employee = entry.getKey();
            int[] monthlyProposal = entry.getValue();

            if (availableEmployees.contains(employee) || monthlyProposal[day.getDayOfMonth()-1] == 0) continue;

            int proposalsCount = 0;
            for (int dayValue : monthlyProposal) {
                proposalsCount += dayValue;
            }

            employeeProposalDayOffCount.put(employee,proposalsCount);
        }

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
        if (employeeWithHighestProposalsCount.isEmpty()){
            scheduleMessageService.addMessage(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z najmniejszą liczbą propozycji dni wolnych",
                            null,
                            day
                    )
            );
            return;
        }

        ScheduleDetails scheduleDetails = scheduleDetailsEntityService.findEmployeeShiftByDay(context.getStoreId(), context.getSchedule().getId(), employeeWithHighestProposalsCount.get(), day);
        scheduleDetailsService.deleteScheduleDetails(context.getStoreId(),context.getSchedule().getId(),scheduleDetails.getId());
        availableEmployees.add(employeeWithHighestProposalsCount.get());

        scheduleMessageService.addMessage(
                context.getSchedule().getId(),
                new CreateScheduleMessageDTO(
                        ScheduleMessageType.INFO,
                        ScheduleMessageCode.TOO_MANY_EMPLOYEE_PROPOSALS,
                        "Propozycja dnia wolnego dla " +
                                employeeWithHighestProposalsCount.get().getFirstName() +
                                " " +
                                employeeWithHighestProposalsCount.get().getLastName() +
                                " na dzień " +
                                day +
                                " została anulowana z powodu zbyt małej liczby dostępnych pracowników. Uzasadnienie: ten pracownik ma najwięcej propozycji dni wolnych.",
                        employeeWithHighestProposalsCount.get().getId(),
                        day
                )
        );
    }
}
