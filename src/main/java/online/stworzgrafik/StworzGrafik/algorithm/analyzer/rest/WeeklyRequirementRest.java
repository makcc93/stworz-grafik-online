package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class WeeklyRequirementRest {

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

    private static void assignEmployeesToRestDays(ScheduleGeneratorContext context, List<Employee> employees, PeriodDateDTO periodDateDTO, Map<LocalDate, Double> daysScoring) {
        LocalDate periodStartDate = periodDateDTO.startDate();
        LocalDate periodEndDate = periodDateDTO.endDate();

        List<Employee> filteredEmployees = employees.stream()
                .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, periodStartDate, periodEndDate))
                .toList();

            for (Employee employee : filteredEmployees) {
                Optional<LocalDate> lowestScoringDate = daysScoring.entrySet().stream()
                        .sorted(Comparator.comparingDouble(
                                Map.Entry::getValue
                        ))
                        .map(Map.Entry::getKey)
                        .findFirst();

                if (lowestScoringDate.isEmpty()) continue;
                if (context.employeeHasProposalShift(employee, lowestScoringDate.get())) continue;

                context.assignEmployeeToRestRequirementDayOff(employee, lowestScoringDate.get());
                daysScoring.merge(lowestScoringDate.get(), 1.0, Double::sum);
            }
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
