package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.RestAnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.RestAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ShiftAnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.DaysOffApplier;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.ProposalShiftApplier;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.VacationApplier;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.CheckoutMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.CreditMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.OpenCloseMatcher;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import online.stworzgrafik.StworzGrafik.fileExport.PdfExport;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
class MonthlyStoreScheduleGenerator {
    private final ScheduleGeneratorContextFactory contextFactory;
    private final WarehousemanScheduleGenerator warehousemanScheduleGenerator;
    private final VacationApplier vacationApplier;
    private final DaysOffApplier daysOffApplier;
    private final ProposalShiftApplier proposalShiftApplier;
    private final DailyShiftGeneratorAlgorithm dailyShiftGeneratorAlgorithm;
    private final EmployeeToShiftMatcher employeeToShiftMatcher;
    private final ExcelExport excelExport;
    private final PdfExport pdfExport;
    private final ScheduleAnalyzer scheduleAnalyzer;
    private final RestAnalyzer restAnalyzer;
    private final EmptyDaysMatcher emptyDaysMatcher;
    private final CreditMatcher creditMatcher;
    private final CheckoutMatcher checkoutMatcher;
    private final OpenCloseMatcher openCloseMatcher;


    @Async
    public void generateMonthlySchedule(Long storeId, Integer year, Integer month) throws IOException {
        ScheduleGeneratorContext context = contextFactory.create(storeId, year, month);

        vacationApplier.applyVacationsToSchedule(context);
        daysOffApplier.applyDaysOffToSchedule(context);
        proposalShiftApplier.applyProposalShiftsToSchedule(context);


        warehousemanScheduleGenerator.generate(context);

        dailyShiftGeneratorAlgorithm.generateShiftsToDays(context);

        employeeToShiftMatcher.matchEmployeeToShift(context);

        creditMatcher.assignForMonth(context);
        checkoutMatcher.assignForMonth(context);
        openCloseMatcher.assignForMonth(context);

        scheduleAnalyzer.analyzeAndResolve(context, LocalDate.now(),new ArrayList<>(),context.getStoreActiveEmployees(), ShiftAnalyzeType.SHIFT_SPLITTER);
        scheduleAnalyzer.analyzeAndResolve(context, LocalDate.now(),new ArrayList<>(),context.getStoreActiveEmployees(), ShiftAnalyzeType.HOURS_SWAPPER);

        emptyDaysMatcher.completeEmptyDaysWithDayOffShift(context);

        restAnalyzer.analyzeAndResolve(context, RestAnalyzeType.WEEKLY_35_HOURS_REST);

        YearMonth ym = YearMonth.of(context.getYear(),context.getMonth());
        for (int day = 1; day <= ym.lengthOfMonth(); day++){
            log.info("");
            LocalDate date = LocalDate.of(context.getYear(),context.getMonth(),day);
            for (Employee e : context.getStoreActiveEmployees().stream().filter(Employee::isCanOpenCloseStore).toList()) {

                log.info("{}                EMPL: {}, Otwarcie: {}",date, e.getLastName(), context.getEmployeeOpenCloseDays().getOrDefault(e, List.of()).contains(date) ? 1 : 0);
            }
            log.info("");
        }

        byte[] excelExport = this.excelExport.export(context);
        Path filePath = Paths.get("/home/mateuszkruk/Pobrane/grafik_" + month + "_" + year + "_" + LocalDateTime.now()+ ".xlsx");
        Files.write(filePath,excelExport);

        byte[] pdfExport = this.pdfExport.export(context);
        Path pdfFilePath = Paths.get("/home/mateuszkruk/Pobrane/grafik_" + month + "_" + year + "_" + LocalDateTime.now()+ ".pdf");
        Files.write(pdfFilePath,pdfExport);
    }
}
