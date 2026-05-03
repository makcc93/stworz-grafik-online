package online.stworzgrafik.StworzGrafik.fileExport;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.shift.Shift;

// ── OpenPDF 3.x (org.openpdf) ───────────────────────────────────────────────
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
// ─────────────────────────────────────────────────────────────────────────────

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfExport implements ExportFile {

    private final HolidayManager holidayManager;

    // Kolory
    private static final Color COLOR_HEADER_BG = new Color(220, 220, 220);
    private static final Color COLOR_WEEKEND   = new Color(192, 192, 192);
    private static final Color COLOR_WAREHOUSE = new Color(255, 200, 150);
    private static final Color COLOR_PROPOSAL  = new Color(200, 150, 255);
    private static final Color COLOR_VACATION  = new Color(150, 255, 150);
    private static final Color COLOR_CREDIT    = new Color(100, 200, 200);
    private static final Color COLOR_CHECKOUT  = new Color(150, 150, 255);

    // Czcionki – dobrany kompromis czytelność vs. zmieszczenie na A3
    private static final float TITLE_SIZE  = 11f;
    private static final float HDR_SIZE    =  6.5f;
    private static final float DATA_SIZE   =  6f;
    private static final float STAT_SIZE   =  6.5f;
    private static final float LEGEND_SIZE =  7f;

    // Padding
    private static final float HDR_PAD  = 2.5f;
    private static final float DATA_PAD = 1.5f;

    @Override
    public byte[] export(ScheduleGeneratorContext context) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── 1. Dane ──────────────────────────────────────────────────────
            Set<Employee> employees = context.getFinalSchedule().values().stream()
                    .flatMap(m -> m.keySet().stream())
                    .collect(Collectors.toSet());

            Map<Employee, BigDecimal>      empHours     = context.getEmployeeHours();
            Map<Employee, Integer>         empWorkDays  = context.getWorkingDaysCount();
            Map<Employee, Integer>         empWeekends  = context.getWorkingOnWeekendCount();
            Map<Employee, Integer>         empVacations = context.getVacationDaysCount();
            Map<Employee, List<LocalDate>> empWarehouse = context.getEmployeeWarehouseDays();
            Map<Employee, List<LocalDate>> empCredit    = context.getEmployeeCreditDays();
            Map<Employee, List<LocalDate>> empCheckout  = context.getEmployeeCheckoutDays();
            Map<Employee, List<LocalDate>> empOpenClose = context.getEmployeeOpenCloseDays();

            LinkedHashMap<LocalDate, Map<Employee, Shift>> sched =
                    context.getFinalSchedule().entrySet().stream()
                            .sorted(Comparator.comparingInt(e -> e.getKey().getDayOfMonth()))
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey, Map.Entry::getValue,
                                    (a, b) -> a, LinkedHashMap::new));

            int year        = context.getYear();
            int month       = context.getMonth();
            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

            // ── 2. Dokument: strona 1 = A3 landscape, wąskie marginesy ──────
            float margin = 15f;
            Document document = new Document(
                    PageSize.A3.rotate(), margin, margin, margin, margin);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont  = new Font(Font.HELVETICA, TITLE_SIZE, Font.BOLD);
            Font hdrFont    = new Font(Font.HELVETICA, HDR_SIZE,   Font.BOLD);
            Font dataFont   = new Font(Font.HELVETICA, DATA_SIZE,  Font.NORMAL);
            Font statFont   = new Font(Font.HELVETICA, STAT_SIZE,  Font.BOLD);
            Font openClFont = new Font(Font.HELVETICA, DATA_SIZE,  Font.BOLDITALIC);

            // ── 3. STRONA 1: główna tabela + legenda ─────────────────────────
            Paragraph title = new Paragraph("GRAFIK " + month + "/" + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Proporcje kolumn: imię szerzej, dzień wąski (1.0), stat trochę szerzej (1.3)
            int cols = 1 + daysInMonth + 7;
            PdfPTable mainTable = new PdfPTable(cols);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(0f);

            float[] colWidths = new float[cols];
            colWidths[0] = 2.8f;
            for (int i = 1; i <= daysInMonth; i++)      colWidths[i] = 1.0f;
            for (int i = daysInMonth + 1; i < cols; i++) colWidths[i] = 1.3f;
            mainTable.setWidths(colWidths);

            // Nagłówek
            addHdrCell(mainTable, "Pracownik", hdrFont, COLOR_HEADER_BG);
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate date    = LocalDate.of(year, month, d);
                String    abbr    = date.getDayOfWeek()
                        .getDisplayName(TextStyle.NARROW_STANDALONE, Locale.of("pl"));
                Color     bg      = isWeekendOrHoliday(date) ? COLOR_WEEKEND : COLOR_HEADER_BG;
                PdfPCell  hc      = styledCell(d + "\n" + abbr, hdrFont, bg, Element.ALIGN_CENTER);
                hc.setPadding(HDR_PAD);
                mainTable.addCell(hc);
            }
            // Skrócone nagłówki statystyk (3 znaki) – mieszczą się w wąskiej kolumnie
            String[] statHdrs = {"GDZ", "DNI", "WKD", "URL", "DOS", "RAT", "KAS"};
            for (String s : statHdrs) addHdrCell(mainTable, s, hdrFont, COLOR_HEADER_BG);

            // Wiersze pracowników
            for (Employee emp : employees) {
                PdfPCell nameCell = styledCell(
                        emp.getFirstName() + " " + emp.getLastName(),
                        dataFont, null, Element.ALIGN_LEFT);
                nameCell.setPadding(DATA_PAD);
                mainTable.addCell(nameCell);

                for (int d = 1; d <= daysInMonth; d++) {
                    LocalDate            date    = LocalDate.of(year, month, d);
                    Map<Employee, Shift> dayMap  = sched.getOrDefault(date, new HashMap<>());
                    Shift                shift   = dayMap.getOrDefault(emp, context.getDefaultDaysOffShift());

                    boolean isVac  = false;
                    boolean isWh   = empWarehouse.getOrDefault(emp, List.of()).contains(date);
                    boolean isPrS  = context.employeeHasProposalShift(emp, date);
                    boolean isPrO  = context.employeeHasProposalDaysOff(emp, date);
                    boolean isCred = empCredit.getOrDefault(emp, List.of()).contains(date);
                    boolean isChk  = empCheckout.getOrDefault(emp, List.of()).contains(date);
                    boolean isOC   = empOpenClose.getOrDefault(emp, List.of()).contains(date);

                    String val;
                    if (!sched.containsKey(date)) {
                        val = "w";
                    } else if (shift.getStartHour().getHour() == 0) {
                        if (shift.getEndHour().getHour() == 0) {
                            val = "w";
                        } else if (shift.getEndHour().getHour() == 8) {
                            val = "u"; isVac = true;
                        } else {
                            val = "?";
                        }
                    } else {
                        val = fmt(shift.getStartHour()) + "\n" + fmt(shift.getEndHour());
                    }

                    Font cellFont = isOC ? openClFont : dataFont;
                    mainTable.addCell(dataCell(val, cellFont, date,
                            isVac, isWh, isPrS, isPrO, isCred, isChk));
                }

                mainTable.addCell(statCell(String.valueOf(empHours.getOrDefault(emp, BigDecimal.ZERO)),          statFont));
                mainTable.addCell(statCell(String.valueOf(empWorkDays.getOrDefault(emp, 0)
                        - empVacations.getOrDefault(emp, 0)),                     statFont));
                mainTable.addCell(statCell(String.valueOf(empWeekends.getOrDefault(emp, 0)),        statFont));
                mainTable.addCell(statCell(String.valueOf(empVacations.getOrDefault(emp, 0)),       statFont));
                mainTable.addCell(statCell(String.valueOf(empWarehouse.getOrDefault(emp, List.of()).size()), statFont));
                mainTable.addCell(statCell(String.valueOf(empCredit.getOrDefault(emp, List.of()).size()),    statFont));
                mainTable.addCell(statCell(String.valueOf(empCheckout.getOrDefault(emp, List.of()).size()),  statFont));
            }
            document.add(mainTable);

            // Legenda
            document.add(new Paragraph(" "));
            Font legendFont      = new Font(Font.HELVETICA, LEGEND_SIZE);
            Font legendTitleFont = new Font(Font.HELVETICA, LEGEND_SIZE, Font.BOLD);

            Paragraph legendTitle = new Paragraph("LEGENDA", legendTitleFont);
            legendTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(legendTitle);

            PdfPTable leg = new PdfPTable(2);
            leg.setWidthPercentage(100);
            leg.setWidths(new float[]{0.3f, 2.2f});

            addLegendEntry(leg, "PROPOZYCJA PRAC.",  COLOR_PROPOSAL,  legendFont);
            addLegendEntry(leg, "URLOP",             COLOR_VACATION,  legendFont);
            addLegendEntry(leg, "DOSTAWA",           COLOR_WAREHOUSE, legendFont);
            addLegendEntry(leg, "RATY",              COLOR_CREDIT,    legendFont);
            addLegendEntry(leg, "KASA",              COLOR_CHECKOUT,  legendFont);
            addLegendEntry(leg, "WEEKEND / ŚWIĘTO",  COLOR_WEEKEND,   legendFont);
            addLegendEntry(leg, "OTW/ZAM = kursywa+pogrubienie", null, legendFont);

            leg.setTotalWidth(150); leg.setLockedWidth(true);
            document.add(leg);

            // ── 4. STRONA 2: tabela godzinowa (A4 landscape) ────────────────
            document.setPageSize(PageSize.A4.rotate());
            document.setMargins(margin, margin, margin, margin);
            document.newPage();

            Font hdrFont2  = new Font(Font.HELVETICA, 7f,   Font.BOLD);
            Font dataFont2 = new Font(Font.HELVETICA, 6.5f, Font.NORMAL);

            Paragraph hourTitle = new Paragraph(
                    "Obsada na godzinę – " + month + "/" + year + "\n Nie jest w niej uwzględniony Pracownik ds. Przyjęcia dostaw lub osoba go zastępująca!", titleFont);
            hourTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(hourTitle);
            document.add(new Paragraph(" "));

            PdfPTable hourTable = new PdfPTable(1 + daysInMonth);
            hourTable.setWidthPercentage(100);
            float[] hW = new float[1 + daysInMonth];
            hW[0] = 2.2f;
            for (int i = 1; i <= daysInMonth; i++) hW[i] = 1.0f;
            hourTable.setWidths(hW);

            addHdrCell(hourTable, "Godzina", hdrFont2, COLOR_HEADER_BG);
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate date = LocalDate.of(year, month, d);
                Color bg = isWeekendOrHoliday(date) ? COLOR_WEEKEND : COLOR_HEADER_BG;
                PdfPCell hc = styledCell(String.valueOf(d), hdrFont2, bg, Element.ALIGN_CENTER);
                hc.setPadding(HDR_PAD);
                hourTable.addCell(hc);
            }

            for (int h = 0; h < 24; h++) {
                String   label = String.format("%02d:00-%02d:00", h, h + 1);
                PdfPCell lc    = styledCell(label, dataFont2, null, Element.ALIGN_CENTER);
                lc.setPadding(DATA_PAD);
                hourTable.addCell(lc);

                for (int d = 1; d <= daysInMonth; d++) {
                    LocalDate date   = LocalDate.of(year, month, d);
                    int[]     counts = sumOfDailyShiftsAsArray(context, date, employees);
                    Color     bg     = isWeekendOrHoliday(date) ? COLOR_WEEKEND : null;
                    PdfPCell  nc     = styledCell(String.valueOf(counts[h]), dataFont2, bg, Element.ALIGN_CENTER);
                    nc.setPadding(DATA_PAD);
                    hourTable.addCell(nc);
                }
            }
            document.add(hourTable);

            // ── 5. STRONA 3: wiadomości ──────────────────────────────────────
            document.newPage();

            Paragraph msgTitle = new Paragraph("INFORMACJE DO GRAFIKA", titleFont);
            msgTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(msgTitle);
            document.add(new Paragraph(" "));

            Font msgHdrFont  = new Font(Font.HELVETICA, 8f, Font.BOLD);
            Font msgDataFont = new Font(Font.HELVETICA, 8f, Font.NORMAL);

            PdfPTable msgTable = new PdfPTable(3);
            msgTable.setWidthPercentage(100);
            msgTable.setWidths(new float[]{5f, 2f, 2f});
            addHdrCell(msgTable, "Wiadomość", msgHdrFont, COLOR_HEADER_BG);
            addHdrCell(msgTable, "Data",      msgHdrFont, COLOR_HEADER_BG);
            addHdrCell(msgTable, "Rodzaj",    msgHdrFont, COLOR_HEADER_BG);

            for (CreateScheduleMessageDTO dto : context.getFinalScheduleMessages()) {
                msgTable.addCell(statCell(dto.message(), msgDataFont));
                msgTable.addCell(statCell(
                        dto.messageDate().format(DateTimeFormatter.ISO_DATE), msgDataFont));
                msgTable.addCell(statCell(dto.scheduleMessageType().toString(), msgDataFont));
            }
            document.add(msgTable);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new IOException("Błąd generowania PDF", e);
        }
    }

    // ── Metody pomocnicze ─────────────────────────────────────────────────────

    /** "8:00" zamiast "08:00:00" */
    private String fmt(LocalTime t) {
        return String.format("%d:%02d", t.getHour(), t.getMinute());
    }

    private boolean isWeekendOrHoliday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date);
    }

    private void addHdrCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.4f);
        cell.setPadding(HDR_PAD);
        table.addCell(cell);
    }

    private PdfPCell styledCell(String text, Font font, Color bg, int hAlign) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        if (bg != null) cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(hAlign);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.4f);
        cell.setPadding(DATA_PAD);
        return cell;
    }

    private PdfPCell dataCell(String text, Font font, LocalDate date,
                              boolean isVac, boolean isWh,
                              boolean isPrS, boolean isPrO,
                              boolean isCred, boolean isChk) {
        Color bg = null;
        if (isWh)              bg = COLOR_WAREHOUSE;
        if (isPrS || isPrO)   bg = COLOR_PROPOSAL;
        if (isVac)             bg = COLOR_VACATION;
        if (isCred)            bg = COLOR_CREDIT;
        if (isChk)             bg = COLOR_CHECKOUT;
        if (bg == null && isWeekendOrHoliday(date)) bg = COLOR_WEEKEND;

        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        if (bg != null) cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderWidth(0.4f);
        cell.setPadding(DATA_PAD);
        return cell;
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

    private int[] sumOfDailyShiftsAsArray(
            ScheduleGeneratorContext context, LocalDate date, Set<Employee> employees) {
        Map<Employee, Shift> daily = context.getFinalSchedule().getOrDefault(date, new HashMap<>());
        int[] counts = new int[24];
        for (int i = 0; i < 24; i++) {
            for (Employee emp : employees) {
                if (context.isEmployeeWorkingInWarehouse(emp, date)) continue;
                Shift shift = daily.getOrDefault(emp, context.findShiftByArray(new int[24]));
                if (!emp.isWarehouseman() && !shift.equals(context.getDefaultVacationShift())) {
                    counts[i] += context.shiftAsArray(shift)[i];
                }
            }
        }
        return counts;
    }
}