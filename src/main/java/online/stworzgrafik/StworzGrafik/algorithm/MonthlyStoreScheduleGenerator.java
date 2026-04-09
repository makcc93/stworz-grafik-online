package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.DaysOffApplier;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.ProposalShiftApplier;
import online.stworzgrafik.StworzGrafik.algorithm.proposalsAndVacations.VacationApplier;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

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


    @Async
    public void generateMonthlySchedule(Long storeId, Integer year, Integer month) throws IOException {
        ScheduleGeneratorContext context = contextFactory.create(storeId, year, month);

        vacationApplier.applyVacationsToSchedule(context);
        daysOffApplier.applyDaysOffToSchedule(context);
        proposalShiftApplier.applyProposalShiftsToSchedule(context);

        warehousemanScheduleGenerator.generate(context);

        dailyShiftGeneratorAlgorithm.generateShiftsToDays(context);

        employeeToShiftMatcher.matchEmployeeToShift(context);

        byte[] export = excelExport.export(context);
        Path filePath = Paths.get("/home/mateuszkruk/Pobrane/grafik_" + month + "_" + year + "_" + LocalDateTime.now()+ ".xlsx");
        Files.write(filePath,export);
    }
}
