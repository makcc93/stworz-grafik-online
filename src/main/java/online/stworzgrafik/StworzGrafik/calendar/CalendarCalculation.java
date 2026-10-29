package online.stworzgrafik.StworzGrafik.calendar;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.workingNorm.WorkingNormService;
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
    private final StandardWorkingHoursCalculator standardWorkingHoursCalculator;
    private final WorkingNormService workingNormService;

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
        return standardWorkingHoursCalculator.getMonthlyMaxWorkingDays(year, month);
    }

    public int getMonthlyWorkingHours(int year, int month){
        return workingNormService.getHours(year, month);
    }

    public BigDecimal getMonthlyNormForEmployee(int year, int month, Employee employee) {
        BigDecimal baseNorm = BigDecimal.valueOf(getMonthlyWorkingHours(year, month));
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

    /**
     * Zwraca liczbę godzin, jaką pojedynczy dzień urlopu lub delegacji powinien wnieść
     * do grafiku danego pracownika: średnia norma dzienna (8h, lub własna norma
     * pracownika specjalnego z pola maxDailyHours) pomnożona przez wymiar etatu.
     */
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