package online.stworzgrafik.StworzGrafik.shift;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long>, JpaSpecificationExecutor<Shift> {
    boolean existsByStartHourAndEndHour(LocalTime startHour, LocalTime endHour);
    Optional<Shift> findByStartHourAndEndHour(LocalTime startHour, LocalTime endHour);
}
