package online.stworzgrafik.StworzGrafik.calendar;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public BigDecimal getMonthlyNormForEmployee(int year, int month, Employee employee) {
        BigDecimal baseNorm = BigDecimal.valueOf(getMonthlyStandardWorkingHours(year, month));
        BigDecimal etatMultiplier = getEtatMultiplier(employee);

        BigDecimal norm = baseNorm.multiply(etatMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // jeśli special — bierzemy minimum z normy etatowej i normy specjalnej
        if (Boolean.TRUE.equals(employee.getIsSpecial()) && employee.getSpecialWorkNorm() != null) {
            BigDecimal workingDays = BigDecimal.valueOf(getMonthlyMaxWorkingDays(year, month));
            BigDecimal weeksInMonth = workingDays.divide(BigDecimal.valueOf(5), 4, RoundingMode.HALF_UP);

            BigDecimal specialNorm = employee.getSpecialWorkNorm().getWeeklyNorm()
                    .multiply(weeksInMonth)
                    .multiply(etatMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            return norm.min(specialNorm);
        }

        return norm;
    }

    public BigDecimal getDailyNormForEmployee(Employee employee) {
        BigDecimal baseDailyNorm = (Boolean.TRUE.equals(employee.getIsSpecial()) && employee.getSpecialWorkNorm() != null)
                ? employee.getSpecialWorkNorm().getMaxDailyHours()
                : BigDecimal.valueOf(8);

        BigDecimal etatMultiplier = getEtatMultiplier(employee);

        return baseDailyNorm.multiply(etatMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getEtatMultiplier(Employee employee) {
        int numerator = employee.getEtatNumerator() != null ? employee.getEtatNumerator() : 1;
        int denominator = employee.getEtatDenominator() != null ? employee.getEtatDenominator() : 1;

        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }
}