package online.stworzgrafik.StworzGrafik.calendar.workingNorm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingNormRepository extends JpaRepository<WorkingNorm, Long> {
    Optional<WorkingNorm> findByYearAndMonth(int year, int month);

    List<WorkingNorm> findAllByYear(int year);
}