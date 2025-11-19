package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ShiftTypeConfigRepository extends JpaRepository<ShiftTypeConfig,Long>, JpaSpecificationExecutor<ShiftTypeConfig> {
    Optional<ShiftTypeConfig> findByCode(ShiftCode code);

    @Query("SELECT s.defaultHours FROM ShiftTypeConfig s WHERE s.code = :code")
    Optional<BigDecimal> getDefaultHours(ShiftCode code);

    @Query("SELECT s.countsAsWork FROM ShiftTypeConfig s WHERE s.code = :code")
    Boolean countsAsWork(ShiftCode code);
}
