package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageCode;
import online.stworzgrafik.StworzGrafik.schedule.message.ScheduleMessageType;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyThirtyFiveHoursRestAnalyzer implements RestAnalyzerStrategy{

    private static final int MINIMUM_WEEKLY_REST_HOURS = 35;

    @Override
    public RestAnalyzeType getSupportedType() {
        return RestAnalyzeType.WEEKLY_35_HOURS_REST;
    }

    @Override
    public RestAnalyzerResult analyze(ScheduleGeneratorContext context) {
        LinkedHashMap<LocalDate, Map<Employee, Shift>> finalSchedule = context.getFinalSchedule();
        Map<Integer, PeriodDateDTO> periodWeek = context.getPeriodWeek();
        List<WeeklyThirtyFiveHoursRestResult.Violation> violations = new ArrayList<>();

        for (Map.Entry<Integer, PeriodDateDTO> entry: periodWeek.entrySet()){
            Map<Employee,int[]> employeeWeeklyShiftCountAsArray = new HashMap<>();

            Integer weekIndex = entry.getKey();
            PeriodDateDTO periodDateDTO = entry.getValue();

            LocalDate periodStartDate = periodDateDTO.startDate();
            LocalDate periodEndDate = periodDateDTO.endDate();

            if (periodEndDate.getDayOfMonth() - periodStartDate.getDayOfMonth() < 3 ) continue;


            LocalDate currentDate = periodStartDate;
            while (!currentDate.isAfter(periodEndDate)){
                for(Map.Entry<Employee,Shift> employeeShiftEntry : finalSchedule.getOrDefault(currentDate,new HashMap<>()).entrySet()){
                    Employee employee = employeeShiftEntry.getKey();
                    Shift shift = employeeShiftEntry.getValue();

                    int[] currentShiftCount = employeeWeeklyShiftCountAsArray.getOrDefault(employee,new int[0]);

                    int[] shiftAsArray = shift.equals(context.getDefaultVacationShift()) ? new int[24] : context.shiftAsArray(shift);

                    int[] updatedShiftCount = IntStream.concat(IntStream.of(currentShiftCount),IntStream.of(shiftAsArray)).toArray();

                    employeeWeeklyShiftCountAsArray.put(employee,updatedShiftCount);
                }

                currentDate = currentDate.plusDays(1);
            }

            for (Map.Entry<Employee, int[]> shiftArrayEntry : employeeWeeklyShiftCountAsArray.entrySet()){
                Employee employee = shiftArrayEntry.getKey();
                int[] weeklyShiftArray = shiftArrayEntry.getValue();

                int maxFreeHoursInARow = 0;
                int freeHours = 0;
                for (int i = 0; i < weeklyShiftArray.length; i++){
                    if (weeklyShiftArray[i] == 0){
                        freeHours++;

                        if (freeHours > maxFreeHoursInARow){
                            maxFreeHoursInARow = freeHours;
                        }
                    } else {
                        freeHours = 0;
                    }
                }

                log.info("TYDZIEŃ {}, EMPLOYEE {} {}, WeeklyArraySize {}, MAX FREE HOURS IN A ROW: {}", weekIndex,employee.getFirstName(),employee.getLastName(),weeklyShiftArray.length,maxFreeHoursInARow);

                if (maxFreeHoursInARow < MINIMUM_WEEKLY_REST_HOURS){
                    violations.add(new WeeklyThirtyFiveHoursRestResult.Violation(
                            employee, weekIndex, periodStartDate, periodEndDate, maxFreeHoursInARow));
                }
            }

        }
        return new WeeklyThirtyFiveHoursRestResult(violations);
    }

    @Override
    public boolean hasProblem(RestAnalyzerResult result) {
        return !((WeeklyThirtyFiveHoursRestResult) result).violations().isEmpty();
    }

    @Override
    public void resolve(ScheduleGeneratorContext context, RestAnalyzerResult result) {
        WeeklyThirtyFiveHoursRestResult restResult = (WeeklyThirtyFiveHoursRestResult) result;

        for (WeeklyThirtyFiveHoursRestResult.Violation violation : restResult.violations()){
            Employee employee = violation.employee();

            log.warn("Pracownik {} {} nie ma zagwarantowanych {}h nieprzerwanego odpoczynku w tygodniu {} ({} - {}) - maksymalny nieprzerwany odpoczynek wyniósł {}h",
                    employee.getFirstName(), employee.getLastName(), MINIMUM_WEEKLY_REST_HOURS,
                    violation.weekIndex(), violation.weekStart(), violation.weekEnd(), violation.maxFreeHoursInARow());

            context.registerMessageOnSchedule(new CreateScheduleMessageDTO(
                    ScheduleMessageType.WARNING,
                    ScheduleMessageCode.EMPLOYEE_WEEKLY_REST_BELOW_35_HOURS,
                    "Pracownik " + employee.getFirstName() + " " + employee.getLastName() +
                            " ma w tygodniu " + violation.weekStart() + " - " + violation.weekEnd() +
                            " tylko " + violation.maxFreeHoursInARow() + "h nieprzerwanego odpoczynku" +
                            " (wymagane " + MINIMUM_WEEKLY_REST_HOURS + "h) - rozważ przesunięcie dnia wolnego.",
                    employee.getId(),
                    violation.weekStart()));
        }
    }
}