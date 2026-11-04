package online.stworzgrafik.StworzGrafik.calendar;

import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.calendar.holidays.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class StandardWorkingHoursCalculator {
    private static final int STANDARD_DAILY_HOURS = 8;

    private final HolidayService holidayService;

    public int getMonthlyMaxWorkingDays(int year, int month) {
        int monthlyWorkingDays = 0;

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate date = yearMonth.atDay(1);

        while (date.getMonthValue() == month) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    date.getDayOfWeek() != DayOfWeek.SUNDAY &&
                    !holidayService.isHoliday(date)) {
                monthlyWorkingDays++;
            }

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY &&
                    holidayService.isHoliday(date)) {
                monthlyWorkingDays--;
            }

            date = date.plusDays(1);
        }

        log.info("Montly Max Working Days = {}",monthlyWorkingDays);
        return monthlyWorkingDays;
    }

    public int getDefaultMonthlyHours(int year, int month) {
        return getMonthlyMaxWorkingDays(year, month) * STANDARD_DAILY_HOURS;
    }
}