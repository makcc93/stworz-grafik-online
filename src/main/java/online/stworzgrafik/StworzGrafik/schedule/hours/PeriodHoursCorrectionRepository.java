package online.stworzgrafik.StworzGrafik.schedule.hours;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface PeriodHoursCorrectionRepository extends JpaRepository<PeriodHoursCorrection, Long> {

    Optional<PeriodHoursCorrection> findByStore_IdAndEmployee_IdAndYearAndMonth(
            Long storeId, Long employeeId, Integer year, Integer month);

    List<PeriodHoursCorrection> findAllByStore_IdAndYearAndMonthIn(
            Long storeId, Integer year, List<Integer> months);
}