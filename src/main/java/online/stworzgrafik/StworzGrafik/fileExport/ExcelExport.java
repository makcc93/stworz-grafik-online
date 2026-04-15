package online.stworzgrafik.StworzGrafik.fileExport;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExport implements ExportFile{
    private final HolidayManager holidayManager;

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
            int columnIndex = 1;
            Integer year = context.getYear();
            Integer month = context.getMonth();
            YearMonth yearMonth = YearMonth.of(year,month);

            Sheet sheet = workbook.createSheet("GRAFIK " + context.getMonth() + "_" + context.getYear());

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Pracownik");
            headerRow.getCell(0).setCellStyle(headerStyle);


            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++){
                LocalDate date = LocalDate.of(year, month, day);

                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue(day + " " + yearMonth.getMonth().toString());
                cell.setCellStyle(defineBackgroundColor(workbook,headerStyle,cell,date));
            }

            Cell totalHoursCell = headerRow.createCell(columnIndex++);
            totalHoursCell.setCellValue("GODZINY");
            totalHoursCell.setCellStyle(headerStyle);

            Cell totalWorkingDaysCell = headerRow.createCell(columnIndex++);
            totalWorkingDaysCell.setCellValue("DNI PRACY");
            totalWorkingDaysCell.setCellStyle(headerStyle);

            int rowNumber = 1;
            Row row = sheet.createRow(rowNumber++);


            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);

                Cell employeeName = row.createCell(day);
                employeeName.setCellValue(yearMonth.atDay(day).getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("pl", "PL")));
                employeeName.setCellStyle(defineBackgroundColor(workbook,dataStyle,employeeName,date));
            }




            for (Employee employee : employees){
                row = sheet.createRow(rowNumber++);
                row.setHeightInPoints(24);

                Cell employeeName = row.createCell(0);
                employeeName.setCellValue(employee.getFirstName() + " " + employee.getLastName());
                employeeName.setCellStyle(dataStyle);

                int cellIndex = 1;
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate date = LocalDate.of(year,month,day);

                    if (!scheduleContainsDate(finalScheduleSortedByDate, date)){
                        Cell cell = row.createCell(cellIndex++);
                        cell.setCellValue("w");
                        cell.setCellStyle(defineBackgroundColor(workbook,dataStyle,cell,date));
                        continue;
                    }

                    Map<Employee, Shift> dailyMap = finalScheduleSortedByDate.getOrDefault(date, new HashMap<>());


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

                        cell.setCellStyle(defineBackgroundColor(workbook,dataStyle,cell,date));
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

            rowNumber++;

            for (int i = 0; i < 24; i++) {
                row = sheet.createRow(rowNumber++); //Row row

                LocalTime hour = LocalTime.of(i, 0);
                Cell employeeName = row.createCell(0);
                employeeName.setCellValue(hour + " - " + hour.plusHours(1));
                employeeName.setCellStyle(dataStyle);

                int cellIndex = 1;
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate date = LocalDate.of(year, month, day);
                    int[] dailyShiftsCount = sumOfDailyShiftsAsArray(context,date,employees);

                    Cell cell = row.createCell(cellIndex++);
                    cell.setCellValue(dailyShiftsCount[i]);
                    cell.setCellStyle(defineBackgroundColor(workbook,dataStyle,cell,date));
                }
            }

            sheet.autoSizeColumn(0);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        }
    }

    private CellStyle defineBackgroundColor(Workbook workbook, CellStyle originalCellStyle, Cell cell, LocalDate date){

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY || holidayManager.isHoliday(date)){
            CellStyle updatedStyle = workbook.createCellStyle();

            updatedStyle.cloneStyleFrom(originalCellStyle);
            updatedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            updatedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            cell.setCellStyle(updatedStyle);

            return updatedStyle;
        }

        return originalCellStyle;
    }

    private int[] sumOfDailyShiftsAsArray(ScheduleGeneratorContext context, LocalDate date, Set<Employee> employees){
        Map<Employee, Shift> dailySchedule = context.getFinalSchedule().getOrDefault(date, new HashMap<>());
        int[] shiftCount = new int[24];
        for (int i = 0; i < 24; i++) {
            for (Employee employee : employees) {
                Shift shift = dailySchedule.getOrDefault(employee, context.findShiftByArray(new int[24]));

                if (!employee.isWarehouseman() && !shift.equals(context.getDefaultVacationShift())) {

                    shiftCount[i] += context.shiftAsArray(shift)[i];
                }
            }
        }
        return shiftCount;
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
//        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
        style.setAlignment(HorizontalAlignment.RIGHT);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
