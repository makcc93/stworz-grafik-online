package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyThirtyFiveHoursRestAnalyzer implements RestAnalyzerStrategy{
    @Override
    public RestAnalyzeType getSupportedType() {
        return RestAnalyzeType.WEEKLY_35_HOURS_REST;
    }

    @Override
    public RestAnalyzerResult analyze(ScheduleGeneratorContext context) {
        LinkedHashMap<LocalDate, Map<Employee, Shift>> finalSchedule = context.getFinalSchedule();
        Map<Integer, PeriodDateDTO> periodWeek = context.getPeriodWeek();

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

                    int[] currentShiftCount = employeeWeeklyShiftCountAsArray
                            .computeIfAbsent(employee, k -> new int[24]);

                    for (int i = 0; i < currentShiftCount.length; i++) {
                        currentShiftCount[i] += context.shiftAsArray(shift)[i];
                    }

                }
                currentDate = currentDate.plusDays(1);
            }

            employeeWeeklyShiftCountAsArray.forEach((empl, shiftCount) -> {
                log.info("TYDZIEN: {}, Pracownik: {}-{}, SumaZmian: {}", weekIndex, empl.getFirstName(), empl.getLastName(),Arrays.toString(shiftCount));
            });
        }

        return new WeeklyThirtyFiveHoursRestResult(new int[24]); //ok dziala ale trzeba zrobic nie sume tylko dodanie do siebie tych zmian i urlopy potraktowac jak 0, a nie 1
    }

    @Override
    public boolean hasProblem(RestAnalyzerResult result) {
        return true;
    }

    @Override
    public void resolve(ScheduleGeneratorContext context, RestAnalyzerResult result) {

    }
}
