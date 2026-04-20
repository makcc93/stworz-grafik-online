package online.stworzgrafik.StworzGrafik.fileExport;

import de.focus_shift.jollyday.core.HolidayManager;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.ScheduleGeneratorContext;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
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
import java.time.format.DateTimeFormatter;
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
        Map<Employee, Integer> employeeWorkingWeekendsCount = context.getWorkingOnWeekendCount();
        Map<Employee, Integer> employeeVacationsCount = context.getVacationDaysCount();
        Map<Employee, List<LocalDate>> employeeWarehouseCount = context.getEmployeeInWarehouse();

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
                cell.setCellStyle(determineCellStyle(workbook,headerStyle,date,false,false,false,false));
            }

            Cell totalHoursCell = headerRow.createCell(columnIndex++);
            totalHoursCell.setCellValue("GODZINY");
            totalHoursCell.setCellStyle(headerStyle);

            Cell totalWorkingDaysCell = headerRow.createCell(columnIndex++);
            totalWorkingDaysCell.setCellValue("DNI PRACY");
            totalWorkingDaysCell.setCellStyle(headerStyle);


            Cell totalWorkingWeekendsCell = headerRow.createCell(columnIndex++);
            totalWorkingWeekendsCell.setCellValue("WEEKENDY");
            totalWorkingWeekendsCell.setCellStyle(headerStyle);

            Cell totalVacationsCell = headerRow.createCell(columnIndex++);
            totalVacationsCell.setCellValue("URLOP");
            totalVacationsCell.setCellStyle(headerStyle);

            Cell totalWarehouseCell = headerRow.createCell(columnIndex++);
            totalWarehouseCell.setCellValue("DOSTAWY");
            totalWarehouseCell.setCellStyle(headerStyle);

            int rowNumber = 1;
            Row row = sheet.createRow(rowNumber++);


            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);

                Cell employeeName = row.createCell(day);
                employeeName.setCellValue(yearMonth.atDay(day).getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("pl", "PL")));
                employeeName.setCellStyle(determineCellStyle(workbook,dataStyle,date,false,false,false,false));
            }




            for (Employee employee : employees){
                row = sheet.createRow(rowNumber++);
                row.setHeightInPoints(24);

                Cell employeeName = row.createCell(0);
                employeeName.setCellValue(employee.getFirstName() + " " + employee.getLastName());
                employeeName.setCellStyle(dataStyle);

                int cellIndex = 1;
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    LocalDate date = LocalDate.of(year, month, day);

                    Cell cell = row.createCell(cellIndex++);

                    if (!scheduleContainsDate(finalScheduleSortedByDate, date)) {
                        cell.setCellValue("w");
                        cell.setCellStyle(determineCellStyle(workbook, dataStyle, date, false, false,false,false));
                        continue;
                    }

                    Map<Employee, Shift> dailyMap = finalScheduleSortedByDate.getOrDefault(date, new HashMap<>());
                    Shift shift = dailyMap.getOrDefault(employee, context.getDefaultDaysOffShift());

                    boolean isVacation = false;
                    boolean isWarehouse = employeeWarehouseCount
                            .getOrDefault(employee, List.of())
                            .contains(date);
                    boolean isShiftProposal = context.employeeHasProposalShift(employee,date);
                    boolean isDayOffProposal = context.employeeHasProposalDaysOff(employee,date);

                    if (shift.getStartHour().getHour() == 0) {
                        if (shift.getEndHour().getHour() == 0) {
                            cell.setCellValue("w");
                        } else if (shift.getEndHour().getHour() == 8) {
                            cell.setCellValue("u");
                            isVacation = true;
                        }
                    } else {
                        cell.setCellValue(shift.getStartHour() + "\n" + shift.getEndHour());
                    }

                    cell.setCellStyle(determineCellStyle(workbook, dataStyle, date, isVacation, isWarehouse,isShiftProposal,isDayOffProposal));
                }

                Integer workedHours = employeeHours.getOrDefault(employee, 0);
                Cell workedHoursCell = row.createCell(cellIndex++);
                workedHoursCell.setCellValue(workedHours);
                workedHoursCell.setCellStyle(totalStyle);

                Integer workedDays = employeeWorkingDaysCount.getOrDefault(employee, 0) - employeeVacationsCount.getOrDefault(employee,0);
                Cell workedDaysCell = row.createCell(cellIndex++);
                workedDaysCell.setCellValue(workedDays);
                workedDaysCell.setCellStyle(totalStyle);

                Integer workedWeekends = employeeWorkingWeekendsCount.getOrDefault(employee, 0);
                Cell workedWeekendsCell = row.createCell(cellIndex++);
                workedWeekendsCell.setCellValue(workedWeekends);
                workedWeekendsCell.setCellStyle(totalStyle);

                Integer vacations = employeeVacationsCount.getOrDefault(employee, 0);
                Cell vacationsCell = row.createCell(cellIndex++);
                vacationsCell.setCellValue(vacations);
                vacationsCell.setCellStyle(totalStyle);

                Integer warehouse = employeeWarehouseCount.getOrDefault(employee, new ArrayList<>()).size();
                Cell warehouseCell = row.createCell(cellIndex);
                warehouseCell.setCellValue(warehouse);
                warehouseCell.setCellStyle(totalStyle);
            }

            rowNumber++;

            for (int i = 0; i < 24; i++) {
                row = sheet.createRow(rowNumber++);

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
                    cell.setCellStyle(determineCellStyle(workbook,dataStyle,date,false,false,false,false));
                }
            }
            createLegend(sheet,workbook);

            sheet.autoSizeColumn(0);


            //NEW SHEET
            Sheet messagesSheet = workbook.createSheet("INFORMACJE DO GRAFIKA");
            messagesSheet.createRow(0).createCell(0).setCellValue("Wiadomości");
            List<CreateScheduleMessageDTO> finalScheduleMessages = context.getFinalScheduleMessages();

            int messagesRowIndex = 1;
            for (CreateScheduleMessageDTO dto : finalScheduleMessages){
                messagesSheet.createRow(messagesRowIndex++).createCell(0).setCellValue(dto.message());
            }

            messagesSheet.getRow(0).createCell(1).setCellValue("Data");
            int dateRowIndex = 1;
            for (CreateScheduleMessageDTO dto : finalScheduleMessages){
                messagesSheet.getRow(dateRowIndex++).createCell(1).setCellValue(dto.messageDate().format(DateTimeFormatter.ISO_DATE));
            }

            messagesSheet.getRow(0).createCell(2).setCellValue("Rodzaj");
            int typeRowIndex = 1;
            for (CreateScheduleMessageDTO dto : finalScheduleMessages){
                messagesSheet.getRow(typeRowIndex++).createCell(2).setCellValue(dto.scheduleMessageType().toString());
            }

            messagesSheet.autoSizeColumn(0);
            //

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        }
    }
    private int[] sumOfDailyShiftsAsArray(ScheduleGeneratorContext context, LocalDate date, Set<Employee> employees){
        Map<Employee, Shift> dailySchedule = context.getFinalSchedule().getOrDefault(date, new HashMap<>());

        int[] shiftCount = new int[24];
        for (int i = 0; i < 24; i++) {
            for (Employee employee : employees) {
                if (context.employeeIsInWarehouse(employee,date)) continue;

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
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle determineCellStyle(Workbook workbook,
                                         CellStyle baseStyle,
                                         LocalDate date,
                                         boolean isVacation,
                                         boolean isWarehouse,
                                         boolean isProposal,
                                         boolean isDayOffProposal) {
        IndexedColors color = null;

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || holidayManager.isHoliday(date)) {
            color = IndexedColors.GREY_25_PERCENT;
        }

        if (isWarehouse) {
            color = IndexedColors.LIGHT_ORANGE; // lub LIGHT_TURQUOISE
        }

        if (isProposal || isDayOffProposal){
            color = IndexedColors.VIOLET;
        }

        if (isVacation) {
            color = IndexedColors.SEA_GREEN; // lub LIGHT_BLUE
        }

        if (color == null) return baseStyle;

        CellStyle updatedStyle = workbook.createCellStyle();
        updatedStyle.cloneStyleFrom(baseStyle);
        updatedStyle.setFillForegroundColor(color.getIndex());
        updatedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return updatedStyle;
    }

    private void createLegend(Sheet sheet, Workbook workbook) {
        int startRow = 2;
        int startCol = 38;

        record LegendEntry(String label, IndexedColors color) {}

        List<LegendEntry> entries = List.of(
                new LegendEntry("PROPOZYCJA PRACOWNIKA", IndexedColors.VIOLET),
                new LegendEntry("URLOP", IndexedColors.SEA_GREEN),
                new LegendEntry("DOSTAWA", IndexedColors.LIGHT_ORANGE),
                new LegendEntry("WEEKEND / ŚWIĘTO", IndexedColors.GREY_25_PERCENT)
        );

        for (int i = 0; i < entries.size(); i++) {
            LegendEntry entry = entries.get(i);

            Row row = sheet.getRow(startRow + i);
            if (row == null) {
                row = sheet.createRow(startRow + i);
            }

            CellStyle legendStyle = workbook.createCellStyle();
            legendStyle.setFillForegroundColor(entry.color().getIndex());
            legendStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            legendStyle.setBorderBottom(BorderStyle.THIN);
            legendStyle.setBorderTop(BorderStyle.THIN);
            legendStyle.setBorderLeft(BorderStyle.THIN);
            legendStyle.setBorderRight(BorderStyle.THIN);
            legendStyle.setAlignment(HorizontalAlignment.CENTER);
            legendStyle.setVerticalAlignment(VerticalAlignment.CENTER);

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
