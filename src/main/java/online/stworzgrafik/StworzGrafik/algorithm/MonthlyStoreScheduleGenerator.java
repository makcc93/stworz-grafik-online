package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.RestAnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.RestAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.WeeklyRequirementRest;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ShiftAnalyzeType;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.DaysOffApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.DelegationApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.ProposalShiftApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.VacationApplier;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.CheckoutMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.CreditMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.OpenCloseMatcher;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import online.stworzgrafik.StworzGrafik.fileExport.PdfExport;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Service
public class MonthlyStoreScheduleGenerator {
    private final ScheduleGeneratorContextFactory contextFactory;
    private final WarehousemanScheduleGenerator warehousemanScheduleGenerator;
    private final VacationApplier vacationApplier;
    private final DaysOffApplier daysOffApplier;
    private final DelegationApplier delegationApplier;
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
    private final WeeklyRequirementRest weeklyRequirementRest;
    private final ScheduleDatabaseSaver scheduleDatabaseSaver;

    /**
     * Generuje grafik miesięczny i zwraca plik Excel jako byte[].
     * Nie zapisuje nic na dysk — plik jest zwracany przez HTTP.
     */
    public byte[] generateMonthlySchedule(Long storeId,Integer year, Integer month) throws IOException {
        ScheduleGeneratorContext context = contextFactory.create(storeId, year, month);

        vacationApplier.applyVacationsToSchedule(context);
        delegationApplier.applyDelegationToSchedule(context);
        daysOffApplier.applyDaysOffToSchedule(context);
        proposalShiftApplier.applyProposalShiftsToSchedule(context);

        weeklyRequirementRest.proceed(context);

        warehousemanScheduleGenerator.generate(context);

        dailyShiftGeneratorAlgorithm.generateShiftsToDays(context);

        employeeToShiftMatcher.matchEmployeeToShift(context);

        creditMatcher.assignRolesForMonth(context);
        checkoutMatcher.assignRolesForMonth(context);
        openCloseMatcher.assignRolesForMonth(context);

        scheduleAnalyzer.analyzeAndResolve(context, LocalDate.now(), new ArrayList<>(), context.getStoreActiveEmployees(), ShiftAnalyzeType.SHIFT_SPLITTER);
        scheduleAnalyzer.analyzeAndResolve(context, LocalDate.now(), new ArrayList<>(), context.getStoreActiveEmployees(), ShiftAnalyzeType.HOURS_SWAPPER);

        dailyShiftGeneratorAlgorithm.modifyShiftsHours(context);

        emptyDaysMatcher.completeEmptyDaysWithDayOffShift(context);

        restAnalyzer.analyzeAndResolve(context, RestAnalyzeType.WEEKLY_35_HOURS_REST);

        scheduleAnalyzer.analyzeAndResolve(context, LocalDate.now(), new ArrayList<>(), context.getStoreActiveEmployees(), ShiftAnalyzeType.SHIFT_SWAPPER);

        creditMatcher.reassignRolesForMonth(context);
        checkoutMatcher.reassignRolesForMonth(context);
        openCloseMatcher.reassignRolesForMonth(context);

        scheduleDatabaseSaver.saveScheduleToDatabase(storeId,context);

        return excelExport.export(context);
    }
}