package online.stworzgrafik.StworzGrafik.fileExport;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsEntityService;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generuje plik Excel na podstawie danych zapisanych w bazie danych.
 * Używany przez endpoint GET /export — dane są spójne z tym co widać w ScheduleViewer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportFromDatabase {

    private final HolidayManager holidayManager;
    private final ScheduleEntityService scheduleEntityService;
    private final ScheduleDetailsEntityService scheduleDetailsEntityService;
    private final CalendarCalculation calendarCalculation;

    public byte[] export(Long storeId, Long scheduleId) throws IOException {
        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);
        Integer year  = schedule.getYear();
        Integer month = schedule.getMonth();
        YearMonth yearMonth = YearMonth.of(year, month);

        List<ScheduleDetails> allDetails = scheduleDetailsEntityService
                .findEntityByCriteria(storeId, scheduleId, new ScheduleDetailsSpecificationDTO(null, null, null, null, null), PageRequest.of(0, 10000))
                .getContent();

        // Kolejność pracowników — unikalna lista w kolejności pierwszego wystąpienia
        List<Long> employeeIds = allDetails.stream()
                .map(d -> d.getEmployee().getId())
                .distinct()
                .collect(Collectors.toList());

        // employeeId → (dayOfMonth → ScheduleDetails)
        Map<Long, Map<Integer, ScheduleDetails>> empDayMap = new LinkedHashMap<>();
        for (Long empId : employeeIds) {
            empDayMap.put(empId, new TreeMap<>());
        }
        for (ScheduleDetails d : allDetails) {
            int day = d.getDate().getDayOfMonth();
            empDayMap.get(d.getEmployee().getId()).put(day, d);
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("GRAFIK " + month + "_" + year);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle   = createDataStyle(workbook);
            CellStyle totalStyle  = createTotalStyle(workbook);

            Row headerRow = sheet.createRow(0);
            createStyledCell(headerRow, 0, "Pracownik", headerStyle);

            int colIdx = 1;
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(day + " " + yearMonth.getMonth());
                cell.setCellStyle(determineCellStyle(workbook, headerStyle, date, null));
            }

            createStyledCell(headerRow, colIdx++, "GODZINY",   headerStyle);
            createStyledCell(headerRow, colIdx++, "DNI PRACY", headerStyle);
            createStyledCell(headerRow, colIdx++, "WEEKENDY",  headerStyle);
            createStyledCell(headerRow, colIdx,   "URLOP",     headerStyle);

            Row dowRow = sheet.createRow(1);
            dowRow.createCell(0).setCellStyle(dataStyle);
            colIdx = 1;
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                Cell cell = dowRow.createCell(colIdx++);
                cell.setCellValue(date.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.of("pl", "PL")));
                cell.setCellStyle(determineCellStyle(workbook, dataStyle, date, null));
            }

            int rowNum = 2;
            for (Long empId : employeeIds) {
                Map<Integer, ScheduleDetails> dayMap = empDayMap.get(empId);
                if (dayMap.isEmpty()) continue;

                ScheduleDetails firstEntry = dayMap.values().iterator().next();
                String fullName = firstEntry.getEmployee().getFirstName()
                        + " " + firstEntry.getEmployee().getLastName();

                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(24);
                createStyledCell(row, 0, fullName, dataStyle);

                BigDecimal totalHours = BigDecimal.ZERO;
                int workDays          = 0;
                int weekendWorkDays   = 0;
                int vacationDays      = 0;

                colIdx = 1;
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate date    = LocalDate.of(year, month, day);
                    boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                            || date.getDayOfWeek() == DayOfWeek.SUNDAY;

                    Cell cell = row.createCell(colIdx++);
                    ScheduleDetails detail = dayMap.get(day);

                    if (detail == null) {
                        cell.setCellValue("w");
                        cell.setCellStyle(determineCellStyle(workbook, dataStyle, date, null));
                        continue;
                    }

                    ShiftCode code = detail.getShiftTypeConfig().getCode();

                    switch (code) {
                        case DAY_OFF    -> cell.setCellValue("w");
                        case VACATION   -> cell.setCellValue("u");
                        case SICK_LEAVE -> cell.setCellValue("L4");
                        case DELEGATION -> cell.setCellValue("d");
                        default -> cell.setCellValue(
                                detail.getShift().getStartHour() + "\n" + detail.getShift().getEndHour()
                        );
                    }

                    cell.setCellStyle(determineCellStyle(workbook, dataStyle, date, code));

                    switch (code) {
                        case WORK, WORK_BY_PROPOSAL -> {
                            BigDecimal shiftHours = computeShiftHours(
                                    detail.getShift().getStartHour(),
                                    detail.getShift().getEndHour()
                            );
                            totalHours = totalHours.add(shiftHours);
                            workDays++;
                            if (isWeekend) weekendWorkDays++;
                        }
                        case VACATION, DELEGATION -> {
                            // Urlop i delegacja wnoszą do grafiku tyle godzin, ile wynosi
                            // indywidualna norma dzienna pracownika (norma bazowa lub własna,
                            // pomnożona przez wymiar etatu) — a nie sztywną wartość defaultHours
                            // z konfiguracji typu zmiany (jednakową dla wszystkich pracowników)
                            // ani, w przypadku delegacji, zero godzin.
                            BigDecimal dailyNorm = calendarCalculation.getDailyNormForEmployee(detail.getEmployee());
                            totalHours = totalHours.add(dailyNorm);
                            if (code == ShiftCode.VACATION) vacationDays++;
                        }
                        case SICK_LEAVE -> {
                            BigDecimal defaultH = detail.getShiftTypeConfig().getDefaultHours();
                            if (defaultH != null) {
                                totalHours = totalHours.add(defaultH);
                            }
                        }
                        default -> { /* DAY_OFF — nie liczy do godzin */ }
                    }
                }

                Cell hoursCell = row.createCell(colIdx++);
                hoursCell.setCellValue(totalHours.doubleValue());
                hoursCell.setCellStyle(totalStyle);

                Cell daysCell = row.createCell(colIdx++);
                daysCell.setCellValue(workDays);
                daysCell.setCellStyle(totalStyle);

                Cell weekendsCell = row.createCell(colIdx++);
                weekendsCell.setCellValue(weekendWorkDays);
                weekendsCell.setCellStyle(totalStyle);

                Cell vacCell = row.createCell(colIdx);
                vacCell.setCellValue(vacationDays);
                vacCell.setCellStyle(totalStyle);
            }

            createLegend(sheet, workbook);
            sheet.autoSizeColumn(0);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private BigDecimal computeShiftHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) return BigDecimal.ZERO;
        if (start.equals(LocalTime.MIDNIGHT) && end.equals(LocalTime.MIDNIGHT)) return BigDecimal.ZERO;
        int startMin = start.getHour() * 60 + start.getMinute();
        int endMin   = end.getHour()   * 60 + end.getMinute();
        int diff     = endMin - startMin;
        if (diff <= 0) diff += 24 * 60;
        return BigDecimal.valueOf(diff).divide(BigDecimal.valueOf(60));
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle determineCellStyle(Workbook workbook, CellStyle base,
                                         LocalDate date, ShiftCode code) {
        IndexedColors color = null;

        boolean isWeekendOrHoliday = date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date);

        if (isWeekendOrHoliday) color = IndexedColors.GREY_25_PERCENT;

        if (code != null) {
            switch (code) {
                case WORK_BY_PROPOSAL -> color = IndexedColors.VIOLET;
                case VACATION         -> color = IndexedColors.SEA_GREEN;
                case SICK_LEAVE       -> color = IndexedColors.LIGHT_YELLOW;
                case DELEGATION       -> color = IndexedColors.INDIGO;
                default               -> { /* WORK, DAY_OFF — zostaje weekend lub null */ }
            }
        }

        if (color == null) return base;

        CellStyle updated = workbook.createCellStyle();
        updated.cloneStyleFrom(base);
        updated.setFillForegroundColor(color.getIndex());
        updated.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return updated;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private void createLegend(Sheet sheet, Workbook workbook) {
        record LegendEntry(String label, IndexedColors color) {}
        List<LegendEntry> entries = List.of(
                new LegendEntry("PROPOZYCJA PRACOWNIKA", IndexedColors.VIOLET),
                new LegendEntry("URLOP",                 IndexedColors.SEA_GREEN),
                new LegendEntry("CHOROBOWE (L4)",        IndexedColors.LIGHT_YELLOW),
                new LegendEntry("DELEGACJA",             IndexedColors.INDIGO),
                new LegendEntry("WEEKEND / ŚWIĘTO",      IndexedColors.GREY_25_PERCENT)
        );
        int startRow = 2;
        int startCol = 40;
        for (int i = 0; i < entries.size(); i++) {
            LegendEntry entry = entries.get(i);
            Row row = sheet.getRow(startRow + i);
            if (row == null) row = sheet.createRow(startRow + i);

            CellStyle legendStyle = workbook.createCellStyle();
            legendStyle.setFillForegroundColor(entry.color().getIndex());
            legendStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            legendStyle.setBorderBottom(BorderStyle.THIN);
            legendStyle.setBorderTop(BorderStyle.THIN);
            legendStyle.setBorderLeft(BorderStyle.THIN);
            legendStyle.setBorderRight(BorderStyle.THIN);
            legendStyle.setAlignment(HorizontalAlignment.CENTER);
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 9);
            legendStyle.setFont(font);

            Cell cell = row.createCell(startCol);
            cell.setCellValue(entry.label());
            cell.setCellStyle(legendStyle);
            sheet.setColumnWidth(startCol, 30 * 256);
        }
    }
}