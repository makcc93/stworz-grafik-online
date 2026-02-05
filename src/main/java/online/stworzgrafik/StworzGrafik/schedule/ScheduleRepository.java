package online.stworzgrafik.StworzGrafik.schedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;


@Repository
interface ScheduleRepository extends JpaRepository<Schedule,Long>, JpaSpecificationExecutor<Schedule> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"store", "scheduleDetails"})
    Page<Schedule> findAll(@Nullable Specification<Schedule> specification, @NonNull Pageable pageable);

    boolean existsByStoreIdAndYearAndMonth(Long storeId, Integer year, Integer month);

}
