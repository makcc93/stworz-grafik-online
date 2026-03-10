package online.stworzgrafik.StworzGrafik.algorithm;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class MonthlyStoreScheduleGeneratorImpl implements MonthlyStoreScheduleGenerator{
    private final ScheduleGeneratorContextFactory contextFactory;
    private final WarehousemanScheduleGenerator warehousemanScheduleGenerator;

    @Async
    @Override
    public void generateMonthlySchedule(Long storeId, Integer year, Integer month) {
        ScheduleGeneratorContext context = contextFactory.create(storeId, year, month);



        warehousemanScheduleGenerator.generate(context);

        generateDailyShifts();
    }
}
