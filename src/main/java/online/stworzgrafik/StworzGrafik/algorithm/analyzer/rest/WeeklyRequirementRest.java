package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageService;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyRequirementRest {
    private final ScheduleMessageService scheduleMessageService;

    public void proceed(ScheduleGeneratorContext context){
        Map<Integer, PeriodDateDTO> periodWeek = context.getPeriodWeek();
        List<Employee> employees = context.getStoreActiveEmployees().stream()
                .filter(empl -> !empl.isWarehouseman())
                .filter(empl -> !empl.isPok())
                .toList();

        for (Map.Entry<Integer,PeriodDateDTO> entry : periodWeek.entrySet()){
            Integer weekIndex = entry.getKey();
            PeriodDateDTO periodDateDTO = entry.getValue();

            LocalDate periodStartDate = periodDateDTO.startDate();
            LocalDate periodEndDate = periodDateDTO.endDate();

            int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
            int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

            if (periodEndDayOfMonth - periodStartDayOfMonth < 3 ) continue;

            boolean assignedToDayOff = checkZeroDraftRequirementDay(context, periodDateDTO,employees);
            if(assignedToDayOff) continue;

            checkVacationAndDayOffProposal(context, periodDateDTO, employees);

            Map<LocalDate, Double> daysScoring = calculateDatesScoring(context, periodDateDTO);

            checkShiftsProposal(context,employees, periodDateDTO, daysScoring);

            assignEmployeesToRestDays(context, employees, periodDateDTO, daysScoring);
        }

        log.info("ZAPLANOWANE DNI WOLNE PRACOWNIKÓW DLA 35-GODZINNEGO TYGODNIOWEGO ODPOCZYNKU");
        context.getStoreActiveEmployees().forEach(empl -> log.info("Pracownik: {}, Dni: {}",
                empl.getLastName(),
                context.getEmployeeWeeklyRestRequirementDaysOff().getOrDefault(empl, Set.of()).toArray()));
    }

    private void assignEmployeesToRestDays(ScheduleGeneratorContext context, List<Employee> employees, PeriodDateDTO periodDateDTO, Map<LocalDate, Double> daysScoring) {
        LocalDate periodStartDate = periodDateDTO.startDate();
        LocalDate periodEndDate = periodDateDTO.endDate();

        if ((periodEndDate.getDayOfMonth() - 1)  - (periodStartDate.getDayOfMonth() + 1) == 1 &&
                Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(periodStartDate.plusDays(1), new int[24])).sum() > 1){
            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.NO_AVAILABLE_DAY_OFF_FOR_WEEKLY_REST,
                    "W okresie " + periodStartDate + " do " + periodEndDate + " brak dostępnego dnia wolnego zapewniającego 35-godzinny odpoczynek, sprawdź ten okres",
                    null,
                    null)
            );

           return;
        }

        // 1. Filtrujemy pracowników raz przed pętlą
        List<Employee> filteredEmployees = employees.stream()
                .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, periodStartDate, periodEndDate))
                .toList();

        boolean addedAnyRestDay;

        // 2. Wykonujemy pętlę tak długo, jak długo udaje nam się przypisać JAKIKOLWIEK dzień restu.
        // Dzięki temu unikamy twardego licznika (attemptCount) i nieskończonej pętli.
        do {
            addedAnyRestDay = false;

            for (Employee employee : filteredEmployees) {
                // Jeśli pracownik dostał już wolne w innym kroku tej pętli, pomijamy go
                if (context.isEmployeeOnRestRequirementDayOff(employee, periodStartDate, periodEndDate)) {
                    continue;
                }

                // 3. Kluczowa zmiana: Sortujemy WSZYSTKIE dni od najniższego scoringu
                List<LocalDate> sortedDates = daysScoring.entrySet().stream()
                        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                        .map(Map.Entry::getKey)
                        .toList();

                // 4. Szukamy PIERWSZEGO dnia, który ten konkretny pracownik może przyjąć
                for (LocalDate candidateDate : sortedDates) {
                    if (context.employeeHasProposalShift(employee, candidateDate)) continue;
                    if (context.employeeIsOnDelegation(employee, candidateDate)) continue;

                    // Znaleźliśmy pasujący dzień o najniższym możliwym scoringu!
                    context.assignEmployeeToRestRequirementDayOff(employee, candidateDate);
                    daysScoring.merge(candidateDate, 1.0, Double::sum);

                    addedAnyRestDay = true;
                    break; // Przypisaliśmy dzień pracownikowi, wychodzimy z pętli dni i idziemy do nast. pracownika
                }
            }

        } while (addedAnyRestDay && !getUnassignedEmployees(context, filteredEmployees, periodStartDate, periodEndDate).isEmpty());
    }

    // Metoda pomocnicza sprawdzająca, czy ktoś jeszcze potrzebuje przypisania
    private static List<Employee> getUnassignedEmployees(ScheduleGeneratorContext context, List<Employee> employees, LocalDate start, LocalDate end) {
        return employees.stream()
                .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, start, end))
                .toList();
    }

    private static Map<LocalDate, Double> calculateDatesScoring(ScheduleGeneratorContext context, PeriodDateDTO dto) {

        LocalDate periodStartDate = dto.startDate();
        LocalDate periodEndDate  = dto.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        Map<LocalDate,Double> daysScoring = new HashMap<>();
        for (int day = (periodStartDayOfMonth +1); day < periodEndDayOfMonth; day++){
            LocalDate currentDate = LocalDate.of(periodStartDate.getYear(), periodStartDate.getMonth(),day);

            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) continue;

            double vacationAndDaysOffValue = 0.00;
            for (Employee employee : context.getStoreActiveEmployees()){
                if (context.employeeIsOnVacation(employee,currentDate)){
                    vacationAndDaysOffValue += 2.00;
                }

                if (context.employeeHasProposalDaysOff(employee,currentDate)){
                    vacationAndDaysOffValue += 2.00;
                }
            }

            double divideBy = 8;
            double dailySum = Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(currentDate, new int[24])).sum();
            double dailyScoring = (dailySum / divideBy) + vacationAndDaysOffValue;

            daysScoring.put(currentDate,dailyScoring);
        }
        return daysScoring;
    }

    private static void checkShiftsProposal(ScheduleGeneratorContext context, List<Employee> employees, PeriodDateDTO dto, Map<LocalDate, Double> daysScoring){
        LocalDate periodStartDate = dto.startDate();
        LocalDate periodEndDate = dto.endDate();

        List<Employee> filteredEmployeesWithShiftProposal = employees.stream()
                .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, periodStartDate, periodEndDate))
                .filter(empl -> context.employeeHasProposalShift(empl,dto))
                .toList();

        for (Employee employee : filteredEmployeesWithShiftProposal){
            for (LocalDate currentDate : daysScoring.keySet()) {
                if (!context.employeeHasProposalShift(employee, currentDate)) {
                    context.assignEmployeeToRestRequirementDayOff(employee, currentDate);
                    daysScoring.merge(currentDate, 1.0, Double::sum);

                    break;
                }
            }
        }
    }


    private static void checkVacationAndDayOffProposal(ScheduleGeneratorContext context, PeriodDateDTO dto, List<Employee> employees) {
        LocalDate periodStartDate = dto.startDate();
        LocalDate periodEndDate = dto.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        for (int day = (periodStartDayOfMonth +1); day < periodEndDayOfMonth; day++) {
            LocalDate currentDate = LocalDate.of(periodStartDate.getYear(), periodStartDate.getMonth(), day);

            List<Employee> filteredEmployees = employees.stream()
                    .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, periodStartDate, periodEndDate))
                    .toList();

            for (Employee employee : filteredEmployees){
                if (context.employeeHasProposalDaysOff(employee,currentDate)){
                    context.assignEmployeeToRestRequirementDayOff(employee,currentDate);
                    continue;
                }

                if (context.employeeIsOnVacation(employee,currentDate)){
                    context.assignEmployeeToRestRequirementDayOff(employee,currentDate);
                }
            }
        }
    }

    private static boolean checkZeroDraftRequirementDay(ScheduleGeneratorContext context, PeriodDateDTO dto, List<Employee> employees) {
        LocalDate periodStartDate = dto.startDate();
        LocalDate periodEndDate = dto.endDate();

        int periodStartDayOfMonth = periodStartDate.getDayOfMonth();
        int periodEndDayOfMonth = periodEndDate.getDayOfMonth();

        for (int day = (periodStartDayOfMonth +1); day < periodEndDayOfMonth; day++){
            LocalDate currentDate = LocalDate.of(periodStartDate.getYear(), periodStartDate.getMonth(),day);

            List<Employee> filteredEmployees = employees.stream()
                    .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, periodStartDate, periodEndDate))
                    .toList();

            int dailyStoreDraftCount = Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(currentDate, new int[24])).sum();
            if (dailyStoreDraftCount == 0){
                for (Employee employee : filteredEmployees) {
                    context.assignEmployeeToRestRequirementDayOff(employee,currentDate);
                }
                return true;
            }
        }
        return false;
    }
}
