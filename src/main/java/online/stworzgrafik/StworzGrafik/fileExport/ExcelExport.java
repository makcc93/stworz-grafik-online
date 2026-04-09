package online.stworzgrafik.StworzGrafik.fileExport;

import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelExport implements ExportFile{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    @Override
    public byte[] export(ScheduleGeneratorContext context) throws IOException {
        Set<Employee> employees = context.getFinalSchedule().values().stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        Map<Employee, Integer> employeeHours = context.getEmployeeHours();
        Map<Employee, Integer> employeeWorkingDaysCount = context.getWorkingDaysCount();
        LinkedHashMap<LocalDate, Map<Employee,Shift>> finalScheduleSortedByDate = context.getFinalSchedule().entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getDayOfMonth()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        try (Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("GRAFIK " + context.getMonth() + "_" + context.getYear());

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Pracownik");
            headerRow.getCell(0).setCellStyle(headerStyle);

            int columnIndex = 1;
            Integer year = context.getYear();
            Integer month = context.getMonth();
            YearMonth yearMonth = YearMonth.of(year,month);
            for (LocalDate date : yearMonth.getMonth()){//finalScheduleSortedByDate.keySet() //todo
                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue(date.format(DATE_FORMATTER));
                cell.setCellStyle(headerStyle);
            }

            Cell totalHoursCell = headerRow.createCell(columnIndex);
            totalHoursCell.setCellValue("Suma godzin");
            totalHoursCell.setCellStyle(headerStyle);

            Cell totalWorkingDaysCell = headerRow.createCell(columnIndex);
            totalWorkingDaysCell.setCellValue("Dni pracy");
            totalWorkingDaysCell.setCellStyle(headerStyle);

            int rowNumber = 1;
            for (Employee employee : employees){
                Row row = sheet.createRow(rowNumber++);

                Cell employeeName = row.createCell(0);
                employeeName.setCellValue(employee.getFirstName() + " " + employee.getLastName());
                employeeName.setCellStyle(dataStyle);

                int cellIndex = 1;
                for (Map.Entry<LocalDate, Map<Employee,Shift>> entry : finalScheduleSortedByDate.entrySet()){
                    LocalDate date = entry.getKey();
                    Map<Employee, Shift> dailyMap = entry.getValue();

                    Shift shift = dailyMap.getOrDefault(employee, context.getDefaultDaysOffShift());

                    Cell cell = row.createCell(cellIndex++);
                    if (shift.getStartHour().getHour() == 0){
                        if (shift.getEndHour().getHour() == 0){
                            cell.setCellValue("w");
                        }
                        else if (shift.getEndHour().getHour() == 8){
                            cell.setCellValue("u");
                        }
                    }
                    else {
                        cell.setCellValue(shift.getStartHour() + "\n" + shift.getEndHour());
                    }

                    cell.setCellStyle(dataStyle);
                }

                Integer workedHours = employeeHours.getOrDefault(employee, 0);
                Cell workedHoursCell = row.createCell(cellIndex);
                workedHoursCell.setCellValue(workedHours);
                workedHoursCell.setCellStyle(totalStyle);

                Integer workedDays = employeeWorkingDaysCount.getOrDefault(employee, 0);
                Cell workedDaysCell = row.createCell(cellIndex);
                workedDaysCell.setCellValue(workedDays);
                workedDaysCell.setCellStyle(totalStyle);
            }

            for (int i = 0; i <= context.getFinalSchedule().size() + 1; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * 1.2));
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
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
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
