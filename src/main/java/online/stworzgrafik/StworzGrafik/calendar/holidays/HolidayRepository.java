package online.stworzgrafik.StworzGrafik.calendar.holidays;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    Optional<Holiday> findByDate(LocalDate date);

    List<Holiday> findAllByDateBetweenOrderByDate(LocalDate from, LocalDate to);

    boolean existsByDateBetween(LocalDate from, LocalDate to);

    // bulk delete wykonuje się natychmiast - Hibernate przy flushu robi inserty PRZED deletami,
    // więc derived delete + ponowne wstawienie tych samych dat złamałoby UNIQUE
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Holiday h where h.date between :from and :to")
    void deleteAllByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}