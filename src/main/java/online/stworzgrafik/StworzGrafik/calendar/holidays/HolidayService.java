package online.stworzgrafik.StworzGrafik.calendar.holidays;

import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayCreateRequest;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayInitializationResponse;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayResponse;
import online.stworzgrafik.StworzGrafik.calendar.holidays.DTO.HolidayUpdateRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Źródło wiedzy o dniach wolnych w całej aplikacji (zastępuje bezpośrednie użycie jollyday HolidayManager).
 * Rok z wpisami w bazie → baza jest źródłem prawdy; rok bez wpisów → fallback do jollyday.
 */
public interface HolidayService {
    /** Czy dzień jest wolny/niehandlowy. Szybkie (cache w pamięci), bezpieczne do wołania w pętlach. */
    boolean isHoliday(LocalDate date);

    /** Dni wolne w danym miesiącu (month: 1-12), posortowane. */
    List<LocalDate> getHolidayDates(int year, int month);

    /** Pełna lista roku do edycji; rok bez wpisów zwraca święta domyślne z persisted = false. */
    List<HolidayResponse> findAllByYear(int year);

    HolidayResponse create(HolidayCreateRequest dto);

    HolidayResponse update(LocalDate date, HolidayUpdateRequest dto);

    void delete(LocalDate date);

    /** Uzupełnia święta domyślne (z jollyday) dla WSZYSTKICH lat 2001-2099, które nie mają jeszcze wpisów. */
    HolidayInitializationResponse initializeDefaults();

    /** Kasuje wpisy roku i wczytuje święta domyślne z jollyday od nowa. */
    List<HolidayResponse> resetYear(int year);
}