package online.stworzgrafik.StworzGrafik.schedule.details;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
interface ScheduleDetailsRepository extends JpaRepository<ScheduleDetails, Long>, JpaSpecificationExecutor<ScheduleDetails> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"employee", "shift", "shiftTypeConfig", "schedule"})
    Page<ScheduleDetails> findAll(@Nullable Specification<ScheduleDetails> specification, @NonNull Pageable pageable);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    Optional<ScheduleDetails> findBySchedule_IdAndEmployee_IdAndDate(Long scheduleId, Long employeeId, LocalDate date);

    List<ScheduleDetails> findBySchedule_IdAndDate(Long scheduleId, LocalDate date);

    @Query("""
    SELECT COALESCE(SUM(
        CASE
            WHEN HOUR(sd.shift.endHour) >= HOUR(sd.shift.startHour)
            THEN HOUR(sd.shift.endHour) - HOUR(sd.shift.startHour)
            ELSE 24 - HOUR(sd.shift.startHour) + HOUR(sd.shift.endHour)
        END
    ), 0)
    FROM ScheduleDetails sd
    WHERE sd.schedule.store.id = :storeId
      AND sd.employee.id = :employeeId
      AND sd.schedule.year = :year
      AND sd.schedule.month = :month
      AND sd.shiftTypeConfig.code IN ('WORK', 'WORK_BY_PROPOSAL')
    """)
    BigDecimal sumHoursByStoreIdAndEmployeeIdAndYearAndMonth(
            @Param("storeId") Long storeId,
            @Param("employeeId") Long employeeId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
