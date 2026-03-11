package online.stworzgrafik.StworzGrafik.algorithm;

import de.jollyday.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class EmployeeToShiftMatcher {
    private final HolidayManager holidayManager;

    public void matchEmployeeToShift(ScheduleGeneratorContext context){
        Map<LocalDate, int[]> everyDayStoreDemandDraft = context.getEveryDayStoreDemandDraftSorted();

        Map<LocalDate, List<Shift>> generatedShiftsByDate = context.getGeneratedShiftsByDate();


        for (Map.Entry<LocalDate, int[]> entry : everyDayStoreDemandDraft.entrySet()) {
            LocalDate date = entry.getKey();

            List<Employee> availableEmployees = context.getStoreActiveEmployees().stream()
                    .filter(empl -> !context.employeeIsOnDayOff(empl,date.getDayOfMonth()))
                    .filter(empl -> !context.employeeIsOnVacation(empl,date.getDayOfMonth()))
                    .filter(empl -> !context.employeeHasProposalShift(empl,date))
                    .toList();

            //koncze na tym ze mam posortwoana liste pracownikow bez tych ktorzy maja urlop, propozycje wolnego i propozycje zmiany (ci juz sa w grafiku)
            //teraz trzeba rozmninic nizej, jak sortowac zmiany jak je dopasowywac do pracownikow


            if (holidayManager.isHoliday(date) || Arrays.stream(everyDayStoreDemandDraft.getOrDefault(date,new int[24])).sum() == 0){
                continue;
            }

            List<Shift> shiftsByStartHour = generatedShiftsByDate.getOrDefault(date, Collections.emptyList()).stream()
                    .sorted(Comparator.comparingInt(
                            s -> s.getStartHour().getHour()
                    ))
                    .toList();


    }
}
