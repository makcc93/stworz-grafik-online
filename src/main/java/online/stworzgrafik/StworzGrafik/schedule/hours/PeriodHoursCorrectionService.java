package online.stworzgrafik.StworzGrafik.schedule.hours;

import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.PeriodHoursCorrectionDTO;
import online.stworzgrafik.StworzGrafik.schedule.hours.DTO.SavePeriodHoursCorrectionsRequest;

import java.math.BigDecimal;
import java.util.List;

public interface PeriodHoursCorrectionService {

    List<PeriodHoursCorrectionDTO> getForStore(Long storeId, Integer year, Integer month);

    void saveCorrections(Long storeId, Integer year, Integer month,
                         SavePeriodHoursCorrectionsRequest request);

    BigDecimal getHoursWorkedSoFarInPeriod(Long storeId, Long employeeId,
                                           Integer year, Integer month);
}