package online.stworzgrafik.StworzGrafik.calendar;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public final class CalendarCalculation{

    public static List<Integer> getDayNumbersByDayOfWeek(int year, int month, DayOfWeek dayOfWeek) {
        List<Integer> result = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate date = yearMonth.atDay(1);

        while (date.getMonthValue() == month){
            if (date.getDayOfWeek() == dayOfWeek){
                result.add(date.getDayOfMonth());
            }

            date = date.plusDays(1);
        }

        return result;
    }
}
