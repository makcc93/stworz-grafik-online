package online.stworzgrafik.StworzGrafik.calendar;

import de.jollyday.HolidayManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CalendarCalculation {
    private final HolidayManager holidayManager;

    public List<Integer> getDayNumbersByDayOfWeek(int year, int month, DayOfWeek dayOfWeek) {
        List<Integer> result = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate date = yearMonth.atDay(1);

        while (date.getMonthValue() == month) {
            if (date.getDayOfWeek() == dayOfWeek) {
                result.add(date.getDayOfMonth());
            }

            date = date.plusDays(1);
        }

        return result;
    }

    public int getMonthlyMaxWorkingDays(int year, int month) {
        int monthlyWorkingDays = 0;

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate date = yearMonth.atDay(1);

        while (date.getMonthValue() == month) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY &&
                date.getDayOfWeek() != DayOfWeek.SUNDAY &&
                !holidayManager.isHoliday(date)) {
                monthlyWorkingDays++;
            }

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY &&
                holidayManager.isHoliday(date)){
                monthlyWorkingDays--;
            }

            date = date.plusDays(1);
        }

        return monthlyWorkingDays;
    }

    public int getMonthlyStandardWorkingHours(int year, int month){
        int monthlyMaxWorkingDays = getMonthlyMaxWorkingDays(year, month);

        return monthlyMaxWorkingDays * 8;
    }
}
