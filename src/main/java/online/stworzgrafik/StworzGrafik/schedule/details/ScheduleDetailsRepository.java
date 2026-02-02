package online.stworzgrafik.StworzGrafik.schedule.details;

import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
interface ScheduleDetailsRepository extends JpaRepository<ScheduleDetails, Long>, JpaSpecificationExecutor<ScheduleDetails> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"employee", "shift", "shiftTypeConfig", "schedule"})
    Page<ScheduleDetails> findAll(@Nullable Specification<ScheduleDetails> specification, @NonNull Pageable pageable);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
