package online.stworzgrafik.StworzGrafik.calendar.holidays.controller;

import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Component
public class HolidayController {
    private final HolidayManager holidayManager;

    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<LocalDate>> getHolidays(@PathVariable Integer year,
                                                       @PathVariable Integer month){
        List<LocalDate> holidays = holidayManager.getHolidays(year).stream()
                .map(Holiday::getDate)
                .filter(date -> date.getMonthValue() == month)
                .collect(Collectors.toList());

        return ResponseEntity.ok(holidays);
    }
}
