package online.stworzgrafik.StworzGrafik.calendar;

import online.stworzgrafik.StworzGrafik.calendar.holidays.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Kalkulator dni roboczych i domyślnej normy – zależy wyłącznie od HolidayService (święta z bazy/jollyday).
 * Wydzielony z CalendarCalculation, żeby WorkingNormService mógł wyliczać wartości domyślne
 * bez cyklicznej zależności (CalendarCalculation -> WorkingNormService -> CalendarCalculation).
 */
@Component
@RequiredArgsConstructor
public class StandardWorkingHoursCalculator {
    private static final int STANDARD_DAILY_HOURS = 8;

    private final HolidayService holidayService;

    /**
     * Liczba dni roboczych w miesiącu: pon-pt bez świąt, a każde święto w sobotę
     * obniża wymiar o jeden dzień (zgodnie z polskim Kodeksem pracy).
     */
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

        return monthlyWorkingDays;
    }

    /** Domyślna norma miesięczna: dni robocze * 8h. */
    public int getDefaultMonthlyHours(int year, int month) {
        return getMonthlyMaxWorkingDays(year, month) * STANDARD_DAILY_HOURS;
    }
}