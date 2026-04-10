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

//            for (LocalDate date : yearMonth.getMonth().length()){//finalScheduleSortedByDate.keySet() //todo
//                Cell cell = headerRow.createCell(columnIndex++);
//                cell.setCellValue(date.format(DATE_FORMATTER));
//                cell.setCellStyle(headerStyle);
//            }

            int columnIndex = 1;
            Integer year = context.getYear();
            Integer month = context.getMonth();
            YearMonth yearMonth = YearMonth.of(year,month);

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){//finalScheduleSortedByDate.keySet() //todo
                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue(day + " " + yearMonth.getMonth().toString());
                cell.setCellStyle(headerStyle);
            }

            Cell totalHoursCell = headerRow.createCell(columnIndex++);
            totalHoursCell.setCellValue("Suma godzin");
            totalHoursCell.setCellStyle(headerStyle);

            Cell totalWorkingDaysCell = headerRow.createCell(columnIndex++);
            totalWorkingDaysCell.setCellValue("Dni pracy");
            totalWorkingDaysCell.setCellStyle(headerStyle);

            int rowNumber = 1;
            for (Employee employee : employees){
                Row row = sheet.createRow(rowNumber++);

                Cell employeeName = row.createCell(0);
                employeeName.setCellValue(employee.getFirstName() + " " + employee.getLastName());
                employeeName.setCellStyle(dataStyle);

                int cellIndex = 1;
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate date = LocalDate.of(year,month,day);

                    if (!scheduleContainsDate(finalScheduleSortedByDate, date)){
                        Cell cell = row.createCell(cellIndex++);
                        cell.setCellValue("w");
                        cell.setCellStyle(dataStyle);
                        continue;
                    }

                    Map<Employee, Shift> dailyMap = finalScheduleSortedByDate.getOrDefault(date, new HashMap<>());

//                    for (Map.Entry<LocalDate, Map<Employee,Shift>> entry : finalScheduleSortedByDate.entrySet()) {
//                        Map<Employee, Shift> dailyMap = entry.getValue();

                        Shift shift = dailyMap.getOrDefault(employee, context.getDefaultDaysOffShift());

                        Cell cell = row.createCell(cellIndex++);
                        if (shift.getStartHour().getHour() == 0) {
                            if (shift.getEndHour().getHour() == 0) {
                                cell.setCellValue("w");
                            } else if (shift.getEndHour().getHour() == 8) {
                                cell.setCellValue("u");
                            }
                        } else {
                            cell.setCellValue(shift.getStartHour() + "\n" + shift.getEndHour());
                        }

                        cell.setCellStyle(dataStyle);
                        }

                Integer workedHours = employeeHours.getOrDefault(employee, 0);
                Cell workedHoursCell = row.createCell(cellIndex++);
                workedHoursCell.setCellValue(workedHours);
                workedHoursCell.setCellStyle(totalStyle);

                Integer workedDays = employeeWorkingDaysCount.getOrDefault(employee, 0);
                Cell workedDaysCell = row.createCell(cellIndex);
                workedDaysCell.setCellValue(workedDays);
                workedDaysCell.setCellStyle(totalStyle);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        }
    }

    private boolean scheduleContainsDate(LinkedHashMap<LocalDate, Map<Employee,Shift>> schedule, LocalDate date){
        for (LocalDate day : schedule.keySet()){
            if (day.isEqual(date)){
                return true;
            }
        }

        return false;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(false);
        font.setFontHeightInPoints((short) 9);
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
