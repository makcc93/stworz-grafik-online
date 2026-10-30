package online.stworzgrafik.StworzGrafik.calendar.holidays.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayCreateRequest;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayInitializationResponse;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayResponse;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayUpdateRequest;
import online.stworzgrafik.StworzGrafik.calendar.holidays.HolidayService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {
    private final HolidayService holidayService;

    // Dostępne dla wszystkich modułów frontendu (kalendarze, draft, grafik, urlopy) - same daty
    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<LocalDate>> getHolidays(@PathVariable Integer year,
                                                       @PathVariable Integer month) {
        return ResponseEntity.ok(holidayService.getHolidayDates(year, month));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{year}")
    ResponseEntity<List<HolidayResponse>> findAllByYear(@PathVariable int year) {
        return ResponseEntity.ok(holidayService.findAllByYear(year));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    ResponseEntity<HolidayResponse> create(@RequestBody @Valid HolidayCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayService.create(request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/{date}")
    ResponseEntity<HolidayResponse> update(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                           @RequestBody @Valid HolidayUpdateRequest request) {
        return ResponseEntity.ok(holidayService.update(date, request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{date}")
    ResponseEntity<Void> delete(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        holidayService.delete(date);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/initialize")
    ResponseEntity<HolidayInitializationResponse> initialize() {
        return ResponseEntity.ok(holidayService.initializeDefaults());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{year}/reset")
    ResponseEntity<List<HolidayResponse>> resetYear(@PathVariable int year) {
        return ResponseEntity.ok(holidayService.resetYear(year));
    }
}