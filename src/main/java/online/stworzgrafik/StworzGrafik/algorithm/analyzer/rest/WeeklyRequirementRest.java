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

            log.info("");
            log.info("###### WEEK INDEX: {}, START: {}({}), END: {}({})", weekIndex,periodStartDate,periodStartDayOfMonth,periodEndDate,periodEndDayOfMonth);
            log.info("");

            boolean assignedToDayOff = checkZeroDraftRequirementDay(context, periodStartDayOfMonth, periodEndDayOfMonth, periodStartDate, periodEndDate,employees);
            if(assignedToDayOff) continue;

            checkVacationAndDayOffProposal(context, periodStartDayOfMonth, periodEndDayOfMonth, periodStartDate, periodEndDate, employees);

            Map<LocalDate, Double> daysScoring = calculateDatesScoring(context, periodStartDayOfMonth, periodEndDayOfMonth, periodStartDate);

            assignEmployeesToRestDays(context, employees, periodStartDate, periodEndDate, daysScoring);
        }

        log.info("");
        context.getStoreActiveEmployees().forEach(empl -> log.info("EEEEEEEEEEEEEEEMPL: {}, Dni: {}",
                empl.getLastName(),
                context.getEmployeeWeeklyRestRequirementDaysOff().getOrDefault(empl, List.of()).toArray()));

        log.info("");
    }

    private static void assignEmployeesToRestDays(ScheduleGeneratorContext context, List<Employee> employees, LocalDate periodStartDate, LocalDate periodEndDate, Map<LocalDate, Double> daysScoring) {
        List<Employee> filteredEmployees = employees.stream()
                .filter(empl -> !context.isEmployeeOnRestRequirementDayOff(empl, periodStartDate, periodEndDate))
                .toList();

        for (Employee employee : filteredEmployees){
            Optional<LocalDate> lowestScoringDate = daysScoring.entrySet().stream()
                    .sorted(Comparator.comparingDouble(
                            Map.Entry::getValue
                    ))
                    .map(Map.Entry::getKey)
                    .findFirst();

            if (lowestScoringDate.isEmpty()){
                log.info("Nie odnaleziono dnia z najniższym scoringiem");
                continue;
            }

            context.assignEmployeeToRestRequirementDayOff(employee,lowestScoringDate.get());

            daysScoring.merge(lowestScoringDate.get(),1.0,Double::sum);
        }
    }

    private static Map<LocalDate, Double> calculateDatesScoring(ScheduleGeneratorContext context, int periodStartDayOfMonth, int periodEndDayOfMonth, LocalDate periodStartDate) {
        Map<LocalDate,Double> daysScoring = new HashMap<>();
        for (int day = (periodStartDayOfMonth +1); day < periodEndDayOfMonth; day++){
            LocalDate currentDate = LocalDate.of(periodStartDate.getYear(), periodStartDate.getMonth(),day);

            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) continue;

            double divideBy = 8;
            double dailySum = Arrays.stream(context.getUneditedOriginalDateStoreDraft().getOrDefault(currentDate, new int[24])).sum();
            double dailyScoring = dailySum / divideBy;

            log.info("!!!!!!!!! DATA: {}, SCORING: {}", currentDate,dailyScoring);
            daysScoring.put(currentDate,dailyScoring);
        }
        return daysScoring;
    }

    private static void checkVacationAndDayOffProposal(ScheduleGeneratorContext context, int periodStartDayOfMonth, int periodEndDayOfMonth, LocalDate periodStartDate, LocalDate periodEndDate, List<Employee> employees) {
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

    private static boolean checkZeroDraftRequirementDay(ScheduleGeneratorContext context, int periodStartDayOfMonth, int periodEndDayOfMonth, LocalDate periodStartDate, LocalDate periodEndDate, List<Employee> employees) {
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
