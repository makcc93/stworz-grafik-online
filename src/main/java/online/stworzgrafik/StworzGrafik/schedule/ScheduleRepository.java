package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
interface ScheduleRepository extends JpaRepository<Schedule,Long>, JpaSpecificationExecutor<Schedule> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"store", "scheduleDetails"})
    Page<Schedule> findAll(@Nullable Specification<Schedule> specification, @NonNull Pageable pageable);

    boolean existsByStoreIdAndYearAndMonth(Long storeId, Integer year, Integer month);

    Optional<Schedule> findByStoreIdAndYearAndMonth(@NotNull Long storeId, @NotNull Integer year, @NotNull Integer month);

}
