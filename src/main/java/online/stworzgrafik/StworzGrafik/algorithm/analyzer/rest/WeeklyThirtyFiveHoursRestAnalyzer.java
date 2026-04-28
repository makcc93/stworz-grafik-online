package online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO.PeriodDateDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

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

                    int[] currentShiftCount = employeeWeeklyShiftCountAsArray.getOrDefault(employee,new int[0]);

                    int[] shiftAsArray = shift.equals(context.getDefaultVacationShift()) ? new int[24] : context.shiftAsArray(shift);

                    int[] updatedShiftCount = IntStream.concat(IntStream.of(currentShiftCount),IntStream.of(shiftAsArray)).toArray();

                    employeeWeeklyShiftCountAsArray.put(employee,updatedShiftCount);
                }

                currentDate = currentDate.plusDays(1);
            }

            employeeWeeklyShiftCountAsArray.forEach((empl, shiftCount) -> {
                log.info("TYDZIEN: {} ({} - {}), Pracownik: {}-{}, Rozmiar tablicy: {}, Suma Tablicy: {}", weekIndex,periodStartDate,periodEndDate, empl.getFirstName(), empl.getLastName(),shiftCount.length, Arrays.stream(shiftCount).sum());
            });

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

                log.info("      WEEK {}, EMPLOYEE {}, WeeklyArraySize {}, MAX FREE HOURS IN A ROW: {}", weekIndex,employee.getLastName(),weeklyShiftArray.length,maxFreeHoursInARow);
            }

        }
        return new WeeklyThirtyFiveHoursRestResult(new int[24]);
    }

    @Override
    public boolean hasProblem(RestAnalyzerResult result) {
        return true;
    }

    @Override
    public void resolve(ScheduleGeneratorContext context, RestAnalyzerResult result) {

    }
}
