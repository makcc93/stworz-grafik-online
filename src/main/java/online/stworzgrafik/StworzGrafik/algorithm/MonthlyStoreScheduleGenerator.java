package online.stworzgrafik.StworzGrafik.algorithm;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface MonthlyStoreScheduleGenerator {
    List<String> generateMonthlySchedule(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month, Pageable pageable);
}
