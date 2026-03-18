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

    @Override
    public AnalyzeType getSupportedType() {
        return AnalyzeType.TOO_MANY_PROPOSALS;
    }

    @Override
    public ScheduleAnalysisResult analyze(ScheduleGeneratorContext context, LocalDate day, List<Shift> shifts, List<Employee> availableEmployees) {
        Map<Employee, int[]> employeeDailyProposals = context.getMonthlyEmployeesProposalShiftsByDate().getOrDefault(day,Collections.emptyMap());
        int[] dailyDraft = context.getUneditedOriginalDateStoreDraft().get(day);
        int[] proposalsCount = getEmployeesDailyProposalCount(employeeDailyProposals);

        return new TooManyProposalsAnalysisResult(availableEmployees,shifts,dailyDraft,proposalsCount);
    }

    @Override
    public boolean hasProblem(ScheduleAnalysisResult result) {
        return ((TooManyProposalsAnalysisResult) result).hasProblem();
    }

    @Override
    public void resolve(ScheduleAnalysisResult result, ScheduleGeneratorContext context, LocalDate day) {
        List<Employee> availableEmployees = ((TooManyProposalsAnalysisResult) result).availableEmployees();
        List<Shift> shifts = ((TooManyProposalsAnalysisResult) result).shifts();
        
        while (shifts.size() > availableEmployees.size()) {
            boolean resolved = cancelProposalAndAddEmployeeToAvailable(availableEmployees, context, day);

            if (!resolved) break;
        }

        int[] dailyDraft = ((TooManyProposalsAnalysisResult) result).dailyDraft();
        int[] proposalsCount = ((TooManyProposalsAnalysisResult) result).proposalsCount();

        for (int indexHour = 0; indexHour < dailyDraft.length; indexHour++){
            while (dailyDraft[indexHour] < proposalsCount[indexHour]){
                boolean resolved = changeProposalToDayOffForMostWorkingEmployee(context, day, indexHour, proposalsCount);

                if (!resolved) break;
            }
            //todo pomysl teraz nad tym, ze powinno sie zaktualizowac draft dzienny jesli usunelismy pracownika ze zmiany na wolne
            //potem przerob addWorkingInformation na metode uniwersalna ktora moglaby cos tez odjac itd z nullami
        }
    }

    private boolean changeProposalToDayOffForMostWorkingEmployee(ScheduleGeneratorContext context, LocalDate day, int indexHour,  int[] proposalsCount) {
        Map<Employee, Integer> employeeHours = context.getEmployeeHours();

        LinkedHashMap<Employee, Integer> employeesSortedByWorkingHoursDesc = employeeHours.entrySet().stream()
                .filter(empl -> {
                    int[] proposal = context.getMonthlyEmployeesProposalShiftsByDate()
                            .getOrDefault(day, Collections.emptyMap())
                            .get(empl.getKey());

                    return proposal != null && proposal[indexHour] > 0;
                    }
                )
                .sorted((key1, key2) -> key2.getValue().compareTo(key1.getValue()))
                .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (keyValue1, keyValue2) -> keyValue1,
                            LinkedHashMap::new
                        )
                );

        Optional<Employee> employeeWithHighestWorkingHours = employeesSortedByWorkingHoursDesc.keySet().stream().findFirst();
        if (employeeWithHighestWorkingHours.isEmpty()){
            scheduleMessageService.addMessage(
                    context.getSchedule().getId(),
                    new CreateScheduleMessageDTO(
                            ScheduleMessageType.WARNING,
                            ScheduleMessageCode.NO_AVAILABLE_EMPLOYEE,
                            "Nie można znaleźć pracownika z największą liczbą przepracowanych godzin",
                            null,
                            day
                    )
            );
            return false;
        }

        ScheduleDetails scheduleDetails = scheduleDetailsEntityService.findEmployeeShiftByDay(
                context.getStoreId(),
                context.getSchedule().getId(),
                employeeWithHighestWorkingHours.get(),
                day
        );

        Shift shift = scheduleDetails.getShift();
        reduceProposalCount(shift,proposalsCount);

        scheduleDetailsService.deleteScheduleDetails(context.getStoreId(), context.getSchedule().getId(),scheduleDetails.getId());
        context.updateEmployeeHours(employeeWithHighestWorkingHours.get(), shift, context.getDefaultDaysOffShift());
        context.updateEmployeeDailyProposal(employeeWithHighestWorkingHours.get(),day,new int[24]);

        scheduleMessageService.addMessage(
                context.getSchedule().getId(),
                new CreateScheduleMessageDTO(
                        ScheduleMessageType.INFO,
                        ScheduleMessageCode.TOO_MANY_EMPLOYEE_PROPOSALS,
                        "Z powodu przekroczenia zapotrzebowania DRAFT w dniu "  + day + " zamiast wpisanej propozycji pracy pracownik: " +
                                employeeWithHighestWorkingHours.get().getFirstName() + " " + employeeWithHighestWorkingHours.get().getLastName() +
                                ", dostał dzień wolny. Uzasadnienie: ten pracownik miał narastająco najwięcej przepracowanych godzin.",
                        employeeWithHighestWorkingHours.get().getId(),
                        day
                )
        );

        return true;
    }

    private void reduceProposalCount(Shift shift, int[] proposalsCount){
        int[] shiftAsArray = shiftEntityService.getShiftAsArray(shift);
        for (int i = 0; i < shiftAsArray.length; i++){
            proposalsCount[i] -= shiftAsArray[i];
        }
    }

    private int[] getEmployeesDailyProposalCount(Map<Employee, int[]> employeeDailyProposals){
        int[] proposalsCount = new int[24];
        for (Employee employee : employeeDailyProposals.keySet()){
            int[] employeeProposal = employeeDailyProposals.get(employee);

            for (int i = 0; i < employeeProposal.length; i++){
                proposalsCount[i] += employeeProposal[i];
            }
        }

        return proposalsCount;
    }
    private boolean cancelProposalAndAddEmployeeToAvailable(List<Employee> availableEmployees, ScheduleGeneratorContext context, LocalDate day){
            Map<Employee, Integer> employeeProposalDayOffCount = getEmployeeProposalsCount(context, day, availableEmployees);

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
                scheduleMessageService.addMessage(
                        context.getSchedule().getId(),
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

            ScheduleDetails scheduleDetails = scheduleDetailsEntityService.findEmployeeShiftByDay(
                    context.getStoreId(),
                    context.getSchedule().getId(),
                    employeeWithHighestProposalsCount.get(),
                    day
            );

            scheduleDetailsService.deleteScheduleDetails(
                    context.getStoreId(),
                    context.getSchedule().getId(),
                    scheduleDetails.getId()
            );

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

            return true;
    }

    private static Map<Employee, Integer> getEmployeeProposalsCount(ScheduleGeneratorContext context, LocalDate day, List<Employee> availableEmployees) {
        Map<Employee, int[]> monthlyEmployeesProposalDayOff = context.getMonthlyEmployeesProposalDayOff();
        Map<Employee, Integer> employeeProposalDayOffCount = new HashMap<>();

        for (Map.Entry<Employee, int[]> entry : monthlyEmployeesProposalDayOff.entrySet()) {
            Employee employee = entry.getKey();
            int[] monthlyProposal = entry.getValue();

            if (availableEmployees.contains(employee) || monthlyProposal[day.getDayOfMonth() - 1] == 0) continue;

            int proposalsCount = 0;
            for (int dayValue : monthlyProposal) {
                proposalsCount += dayValue;
            }

            employeeProposalDayOffCount.put(employee, proposalsCount);
        }
        return employeeProposalDayOffCount;
    }
}
