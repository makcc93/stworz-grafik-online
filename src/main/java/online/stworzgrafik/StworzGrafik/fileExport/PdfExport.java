package online.stworzgrafik.StworzGrafik.fileExport;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.delivery.DayDeliveryConfig;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreWeeklyDeliverySchedule;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfExport {

    private final HolidayManager holidayManager;
    private final ScheduleEntityService scheduleEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final CalendarCalculation calendarCalculation;

    // ── Kolory spójne z ExcelExportFromDatabase ───────────────────────────
    private static final Color COLOR_WEEKEND    = new Color(192, 192, 192); // GREY_25_PERCENT
    private static final Color COLOR_PROPOSAL   = new Color(128, 0, 128);   // VIOLET
    private static final Color COLOR_VACATION   = new Color(46, 139, 87);   // SEA_GREEN
    private static final Color COLOR_SICK_LEAVE = new Color(255, 255, 153); // LIGHT_YELLOW
    private static final Color COLOR_DELEGATION = new Color(75, 0, 130);    // INDIGO
    private static final Color COLOR_DELIVERY   = new Color(255, 255, 0);   // YELLOW — osoba robiąca dostawę

    private static final float TITLE_SIZE  = 11f;
    private static final float HDR_SIZE    = 6.5f;
    private static final float DATA_SIZE   = 6f;
    private static final float STAT_SIZE   = 6.5f;
    private static final float LEGEND_SIZE = 7f;
    private static final float HDR_PAD     = 2.5f;
    private static final float DATA_PAD    = 1.5f;

    public byte[] export(Long storeId, Long scheduleId) throws IOException {
        // ── Pobranie danych z bazy ─────────────────────────────────────────
        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);
        int year  = schedule.getYear();
        int month = schedule.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        List<ScheduleDetails> allDetails = scheduleDetailsEntityService
                .findEntityByCriteria(storeId, scheduleId,
                        new ScheduleDetailsSpecificationDTO(null, null, null, null, null),
                        PageRequest.of(0, 10000))
                .getContent();

        // Ustal kolejność pracowników (wg pierwszego wystąpienia)
        List<Long> employeeIds = allDetails.stream()
                .map(d -> d.getEmployee().getId())
                .distinct()
                .toList();

        // Mapa: employeeId → (dzień miesiąca → ScheduleDetails)
        Map<Long, Map<Integer, ScheduleDetails>> empDayMap = new LinkedHashMap<>();
        for (Long empId : employeeIds) {
            empDayMap.put(empId, new TreeMap<>());
        }
        for (ScheduleDetails d : allDetails) {
            int day = d.getDate().getDayOfMonth();
            empDayMap.get(d.getEmployee().getId()).put(day, d);
        }

        // Mapa: dzień → id pracownika, który tego dnia robi dostawę
        // (magazynier, albo — gdy magazynier jest nieobecny — osoba, która go zastępuje)
        Map<LocalDate, Long> deliveryAssignments = computeDeliveryAssignments(schedule.getStore(), yearMonth, allDetails);

        // ── Budowanie PDF ─────────────────────────────────────────────────
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            float margin = 15f;
            Document document = new Document(PageSize.A3.rotate(), margin, margin, margin, margin);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, TITLE_SIZE, Font.BOLD);
            Font hdrFont   = new Font(Font.HELVETICA, HDR_SIZE,   Font.BOLD);
            Font dataFont  = new Font(Font.HELVETICA, DATA_SIZE,  Font.NORMAL);
            Font statFont  = new Font(Font.HELVETICA, STAT_SIZE,  Font.BOLD);

            // Tytuł
            Paragraph title = new Paragraph("GRAFIK " + month + "/" + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // ── Główna tabela ──────────────────────────────────────────────
            int cols = 1 + daysInMonth + 4; // imię + dni + 4 statystyki
            PdfPTable mainTable = new PdfPTable(cols);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(0f);

            float[] colWidths = new float[cols];
            colWidths[0] = 2.8f;
            for (int i = 1; i <= daysInMonth; i++)      colWidths[i] = 1.0f;
            for (int i = daysInMonth + 1; i < cols; i++) colWidths[i] = 1.3f;
            mainTable.setWidths(colWidths);

            // Nagłówek
            mainTable.addCell(headerCell("Pracownik", hdrFont));
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate date = LocalDate.of(year, month, d);
                String text = d + " " + yearMonth.getMonth().toString();
                Color bg = isWeekendOrHoliday(date) ? COLOR_WEEKEND : null;
                mainTable.addCell(styledCell(text, hdrFont, bg, Element.ALIGN_CENTER, HDR_PAD));
            }
            for (String s : List.of("GODZINY", "DNI PRACY", "WEEKENDY", "URLOP")) {
                mainTable.addCell(headerCell(s, hdrFont));
            }

            // Wiersz z nazwami dni tygodnia
            mainTable.addCell(styledCell("", dataFont, null, Element.ALIGN_CENTER, DATA_PAD));
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate date = LocalDate.of(year, month, d);
                String abbr = date.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.of("pl"));
                Color bg = isWeekendOrHoliday(date) ? COLOR_WEEKEND : null;
                mainTable.addCell(styledCell(abbr, dataFont, bg, Element.ALIGN_CENTER, DATA_PAD));
            }
            for (int i = 0; i < 4; i++) {
                mainTable.addCell(styledCell("", dataFont, null, Element.ALIGN_CENTER, DATA_PAD));
            }

            // Wiersze pracowników
            for (Long empId : employeeIds) {
                Map<Integer, ScheduleDetails> dayMap = empDayMap.get(empId);
                if (dayMap.isEmpty()) continue;

                ScheduleDetails first = dayMap.values().iterator().next();
                String fullName = first.getEmployee().getFirstName() + " " + first.getEmployee().getLastName();
                mainTable.addCell(styledCell(fullName, dataFont, null, Element.ALIGN_LEFT, DATA_PAD));

                BigDecimal totalHours = BigDecimal.ZERO;
                int workDays        = 0;
                int weekendWorkDays = 0;
                int vacationDays    = 0;

                for (int d = 1; d <= daysInMonth; d++) {
                    LocalDate date = LocalDate.of(year, month, d);
                    boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                            || date.getDayOfWeek() == DayOfWeek.SUNDAY;
                    ScheduleDetails detail = dayMap.get(d);

                    String cellText;
                    ShiftCode code;
                    if (detail == null) {
                        cellText = "w";
                        code = null;
                    } else {
                        code = detail.getShiftTypeConfig().getCode();
                        switch (code) {
                            case DAY_OFF    -> cellText = "w";
                            case VACATION   -> cellText = "u";
                            case DELEGATION -> cellText = "d";
                            default -> cellText = fmt(detail.getShift().getStartHour()) + "\n"
                                    + fmt(detail.getShift().getEndHour());
                        }
                    }

                    boolean isDelivery = empId.equals(deliveryAssignments.get(date));
                    Color bg = computeCellBackground(date, code, isDelivery);
                    mainTable.addCell(dataCell(cellText, dataFont, bg));

                    // Statystyki
                    if (code != null) {
                        switch (code) {
                            case WORK, WORK_BY_PROPOSAL -> {
                                BigDecimal hours = computeShiftHours(
                                        detail.getShift().getStartHour(),
                                        detail.getShift().getEndHour());
                                totalHours = totalHours.add(hours);
                                workDays++;
                                if (isWeekend) weekendWorkDays++;
                            }
                            case VACATION, DELEGATION -> {
                                // Urlop i delegacja wnoszą tyle godzin, ile wynosi indywidualna
                                // norma dzienna pracownika (norma bazowa lub własna x wymiar etatu),
                                // a nie sztywna wartość defaultHours ani (dla delegacji) zero godzin.
                                BigDecimal dailyNorm = calendarCalculation.getDailyNormForEmployee(detail.getEmployee());
                                totalHours = totalHours.add(dailyNorm);
                                if (code == ShiftCode.VACATION) vacationDays++;
                            }
                            case SICK_LEAVE -> {
                                BigDecimal defaultH = detail.getShiftTypeConfig().getDefaultHours();
                                if (defaultH != null) totalHours = totalHours.add(defaultH);
                            }
                            default -> { /* DAY_OFF — nie liczy do godzin */ }
                        }
                    }
                }

                mainTable.addCell(statCell(String.valueOf(totalHours.doubleValue()), statFont));
                mainTable.addCell(statCell(String.valueOf(workDays), statFont));
                mainTable.addCell(statCell(String.valueOf(weekendWorkDays), statFont));
                mainTable.addCell(statCell(String.valueOf(vacationDays), statFont));
            }
            document.add(mainTable);

            // ── Legenda ────────────────────────────────────────────────────
            document.add(new Paragraph(" "));
            Font legendFont = new Font(Font.HELVETICA, LEGEND_SIZE);
            Font legendTitleFont = new Font(Font.HELVETICA, LEGEND_SIZE, Font.BOLD);
            Paragraph legTitle = new Paragraph("LEGENDA", legendTitleFont);
            legTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(legTitle);

            PdfPTable legTable = new PdfPTable(2);
            legTable.setWidthPercentage(100);
            legTable.setWidths(new float[]{0.3f, 2.2f});

            addLegendEntry(legTable, "PROPOZYCJA PRAC.",  COLOR_PROPOSAL,   legendFont);
            addLegendEntry(legTable, "URLOP",             COLOR_VACATION,   legendFont);
            addLegendEntry(legTable, "DELEGACJA",         COLOR_DELEGATION, legendFont);
            addLegendEntry(legTable, "OSOBA ROBIĄCA DOSTAWĘ", COLOR_DELIVERY, legendFont);
            addLegendEntry(legTable, "WEEKEND / ŚWIĘTO",  COLOR_WEEKEND,    legendFont);

            legTable.setTotalWidth(150);
            legTable.setLockedWidth(true);
            document.add(legTable);

            // ── Strona 2 – tabela rozkładu godzin ─────────────────────────
            document.setPageSize(PageSize.A4.rotate());
            document.setMargins(margin, margin, margin, margin);
            document.newPage();

            Font hdrFont2  = new Font(Font.HELVETICA, 7f,   Font.BOLD);
            Font dataFont2 = new Font(Font.HELVETICA, 6.5f, Font.NORMAL);

            Paragraph hourTitle = new Paragraph("Rozkład godzin – " + month + "/" + year, titleFont);
            hourTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(hourTitle);
            document.add(new Paragraph(" "));

            PdfPTable hourTable = new PdfPTable(1 + daysInMonth);
            hourTable.setWidthPercentage(100);
            float[] hW = new float[1 + daysInMonth];
            hW[0] = 2.2f;
            for (int i = 1; i <= daysInMonth; i++) hW[i] = 1.0f;
            hourTable.setWidths(hW);

            hourTable.addCell(styledCell("Godzina", hdrFont2, null, Element.ALIGN_CENTER, HDR_PAD));
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate date = LocalDate.of(year, month, d);
                Color bg = isWeekendOrHoliday(date) ? COLOR_WEEKEND : null;
                hourTable.addCell(styledCell(String.valueOf(d), hdrFont2, bg, Element.ALIGN_CENTER, HDR_PAD));
            }

            for (int h = 0; h < 24; h++) {
                String label = String.format("%02d:00 - %02d:00", h, (h + 1) % 24);
                hourTable.addCell(styledCell(label, dataFont2, null, Element.ALIGN_CENTER, DATA_PAD));
                for (int d = 1; d <= daysInMonth; d++) {
                    LocalDate date = LocalDate.of(year, month, d);
                    int count = countEmployeesAtHour(date, h, allDetails, yearMonth);
                    Color bg = isWeekendOrHoliday(date) ? COLOR_WEEKEND : null;
                    hourTable.addCell(styledCell(String.valueOf(count), dataFont2, bg, Element.ALIGN_CENTER, DATA_PAD));
                }
            }
            document.add(hourTable);

            // Brak strony z komunikatami – w eksporcie z bazy nie ma takich danych

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IOException("Błąd generowania PDF", e);
        }
    }

    // ── Pomocnicze metody dla tabeli godzinowej ─────────────────────────────
    private int countEmployeesAtHour(LocalDate date, int hour,
                                     List<ScheduleDetails> allDetails,
                                     YearMonth yearMonth) {
        int count = 0;
        for (ScheduleDetails d : allDetails) {
            if (d.getDate().getDayOfMonth() != date.getDayOfMonth()) continue;
            if (d.getEmployee().isWarehouseman()) continue;
            ShiftCode code = d.getShiftTypeConfig().getCode();
            if (code == ShiftCode.VACATION || code == ShiftCode.DELEGATION) continue;

            LocalTime start = d.getShift().getStartHour();
            LocalTime end   = d.getShift().getEndHour();
            int startIdx = start.getMinute() == 0 ? start.getHour() : start.getHour() + 1;
            int endIdx   = end.getMinute()   == 0 ? end.getHour()   : end.getHour() + 1;

            if (hour >= startIdx && hour < endIdx) {
                count++;
            }
        }
        return count;
    }

    // ── Kolorystyka komórek (taka sama jak w ExcelExportFromDatabase) ──────
    private Color computeCellBackground(LocalDate date, ShiftCode code, boolean isDelivery) {
        if (isDelivery) return COLOR_DELIVERY;

        boolean isWeekendOrHoliday = date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date);

        if (code != null) {
            return switch (code) {
                case WORK_BY_PROPOSAL -> COLOR_PROPOSAL;
                case VACATION         -> COLOR_VACATION;
                case SICK_LEAVE       -> COLOR_SICK_LEAVE;
                case DELEGATION       -> COLOR_DELEGATION;
                default               -> isWeekendOrHoliday ? COLOR_WEEKEND : null;
            };
        }
        return isWeekendOrHoliday ? COLOR_WEEKEND : null;
    }

    // ── Ustalanie kto danego dnia robi dostawę (magazynier lub jego zastępca) ──
    private Map<LocalDate, Long> computeDeliveryAssignments(Store store, YearMonth yearMonth,
                                                            List<ScheduleDetails> allDetails) {
        Map<LocalDate, Long> result = new HashMap<>();
        if (store == null) return result;

        StoreDelivery storeDelivery = store.getDelivery();
        if (storeDelivery == null || !Boolean.TRUE.equals(storeDelivery.getHasDedicatedWarehouseman())) {
            return result;
        }

        Employee warehouseman = storeDelivery.getPrimaryEmployee();
        StoreWeeklyDeliverySchedule weekly = storeDelivery.getStoreWeeklyDeliverySchedule();
        if (warehouseman == null || weekly == null || weekly.getDeliverySchedule() == null) {
            return result;
        }

        Map<LocalDate, List<ScheduleDetails>> byDate = allDetails.stream()
                .collect(Collectors.groupingBy(ScheduleDetails::getDate));

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), day);
            DayDeliveryConfig config = weekly.getDeliverySchedule().get(date.getDayOfWeek());
            if (config == null || !config.hasDelivery()) continue;

            int[] startEnd = startEndHoursFromArray(config.shiftAsArray());
            if (startEnd == null) continue;

            List<ScheduleDetails> dayDetails = byDate.getOrDefault(date, List.of());

            ScheduleDetails warehousemanEntry = dayDetails.stream()
                    .filter(d -> d.getEmployee().getId().equals(warehouseman.getId()))
                    .findFirst().orElse(null);

            if (warehousemanEntry != null
                    && isWorkingShift(warehousemanEntry.getShiftTypeConfig().getCode())
                    && matchesDeliveryHours(warehousemanEntry.getShift(), startEnd)) {
                result.put(date, warehouseman.getId());
                continue;
            }

            // Magazynier nie pracuje tego dnia na dostawie — szukamy, kto go zastępuje
            dayDetails.stream()
                    .filter(d -> !d.getEmployee().getId().equals(warehouseman.getId()))
                    .filter(d -> d.getEmployee().isCanOperateDelivery())
                    .filter(d -> isWorkingShift(d.getShiftTypeConfig().getCode()))
                    .filter(d -> matchesDeliveryHours(d.getShift(), startEnd))
                    .findFirst()
                    .ifPresent(d -> result.put(date, d.getEmployee().getId()));
        }

        return result;
    }

    private boolean isWorkingShift(ShiftCode code) {
        return code == ShiftCode.WORK || code == ShiftCode.WORK_BY_PROPOSAL;
    }

    private boolean matchesDeliveryHours(Shift shift, int[] startEnd) {
        if (shift == null || shift.getStartHour() == null || shift.getEndHour() == null) return false;
        return shift.getStartHour().getHour() == startEnd[0] && shift.getEndHour().getHour() == startEnd[1];
    }

    private int[] startEndHoursFromArray(int[] shiftAsArray) {
        if (shiftAsArray == null || shiftAsArray.length != 24) return null;
        int startHour = -1;
        int endHour = 0;
        for (int i = 0; i < 24; i++) {
            if (shiftAsArray[i] != 0) { startHour = i; break; }
        }
        if (startHour == -1) return null; // brak zmiany dostawy tego dnia
        for (int i = 23; i >= 0; i--) {
            if (shiftAsArray[i] != 0) { endHour = (i + 1) % 24; break; }
        }
        return new int[]{startHour, endHour};
    }

    // ── Narzędzia formatowania ──────────────────────────────────────────────
    private String fmt(LocalTime t) {
        return String.format("%d:%02d", t.getHour(), t.getMinute());
    }

    private BigDecimal computeShiftHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) return BigDecimal.ZERO;
        if (start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT)) return BigDecimal.ZERO;
        int startMin = start.getHour() * 60 + start.getMinute();
        int endMin   = end.getHour()   * 60 + end.getMinute();
        int diff = endMin - startMin;
        if (diff <= 0) diff += 24 * 60;
        return BigDecimal.valueOf(diff).divide(BigDecimal.valueOf(60));
    }

    private boolean isWeekendOrHoliday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date);
    }

    // ── Tworzenie komórek PDF ───────────────────────────────────────────────
    private PdfPCell headerCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.4f);
        cell.setPadding(HDR_PAD);
        return cell;
    }

    private PdfPCell styledCell(String text, Font font, Color bg, int hAlign, float padding) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        if (bg != null) cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(hAlign);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.4f);
        cell.setPadding(padding);
        return cell;
    }

    private PdfPCell dataCell(String text, Font font, Color bg) {
        return styledCell(text, font, bg, Element.ALIGN_CENTER, DATA_PAD);
    }

    private PdfPCell statCell(String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.4f);
        cell.setPadding(DATA_PAD);
        return cell;
    }

    private void addLegendEntry(PdfPTable table, String label, Color color, Font font) {
        PdfPCell colorCell = new PdfPCell();
        colorCell.setBorder(Rectangle.BOX);
        colorCell.setBorderWidth(0.4f);
        colorCell.setFixedHeight(10f);
        if (color != null) colorCell.setBackgroundColor(color);
        table.addCell(colorCell);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setBorderWidth(0.4f);
        labelCell.setPaddingLeft(3f);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(labelCell);
    }
}