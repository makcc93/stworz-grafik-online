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

            boolean assignedToDayOff = checkZeroDraftRequirementDay(context, periodDateDTO,employees);
            if(assignedToDayOff) continue;

            checkVacationAndDayOffProposal(context, periodDateDTO, employees);

            Map<LocalDate, Double> daysScoring = calculateDatesScoring(context, periodDateDTO);

            checkShiftsProposal(context,employees, periodDateDTO, daysScoring);

            assignEmployeesToRestDays(context, employees, periodDateDTO, daysScoring);
        }

        log.info("");
        context.getStoreActiveEmployees().forEach(empl -> log.info("EEEEEEEEEEEEEEEMPL: {}, Dni: {}",
                empl.getLastName(),
                context.getEmployeeWeeklyRestRequirementDaysOff().getOrDefault(empl, Set.of()).toArray()));

        log.info("");
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

                if (lowestScoringDate.isEmpty()) {
                    log.info("Nie odnaleziono dnia z najniższym scoringiem");
                    continue;
                }

                if (context.employeeHasProposalShift(employee, lowestScoringDate.get())) continue;

                context.assignEmployeeToRestRequirementDayOff(employee, lowestScoringDate.get());
                log.info("");
                log.info("LAST_STEP LAST_STEP LAST_STEP DOPISUJE {} DO DATY {}", employee.getLastName(),lowestScoringDate.get());
                log.info("LAST_STEP LAST_STEP LAST_STEP BEFORE DATE {} SCORING: {}", lowestScoringDate.get(),daysScoring.getOrDefault(lowestScoringDate.get(),0.00));
                daysScoring.merge(lowestScoringDate.get(), 1.0, Double::sum);
                log.info("LAST_STEP LAST_STEP LAST_STEP AFTER DATE {} SCORING: {}", lowestScoringDate.get(),daysScoring.getOrDefault(lowestScoringDate.get(),0.00));
                log.info("");
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

            log.info("!!!!!!!!! DATA: {}, SCORING: {} (W TYM WAKACJE/DNI WOLNE: {})", currentDate,dailyScoring, vacationAndDaysOffValue);
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

        log.info("PROPOSALSHIFT_PROPOSALSHIFT_PROPOSALSHIFT EMPLOYEES: {}", filteredEmployeesWithShiftProposal.toArray());

        for (Employee employee : filteredEmployeesWithShiftProposal){
            log.info("PROPOSALSHIFT_PROPOSALSHIFT_PROPOSALSHIFT SPRAWDZAM PRACOWNIKA {}",employee.getLastName());

            for (LocalDate currentDate : daysScoring.keySet()) {
                if (!context.employeeHasProposalShift(employee, currentDate)) {
                    context.assignEmployeeToRestRequirementDayOff(employee, currentDate);
                    log.info("");
                    log.info("PROPOSALSHIFT_PROPOSALSHIFT_PROPOSALSHIFT  DOPISUJE {} DO DATY {} BO NIE MA WTEDY PROPOZYCJI", employee.getLastName(), currentDate);
                    log.info("PROPOSALSHIFT_PROPOSALSHIFT_PROPOSALSHIFT DAY SCORING BEFORE: {}", daysScoring.getOrDefault(currentDate, 0.00));
                    daysScoring.merge(currentDate, 1.0, Double::sum);
                    log.info("PROPOSALSHIFT_PROPOSALSHIFT_PROPOSALSHIFT DAY SCORING AFFTER: {}", daysScoring.getOrDefault(currentDate, 0.00));
                    log.info("");
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
