package online.stworzgrafik.StworzGrafik.store.modificationHours;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface ShiftHourModificationConfigRepository extends JpaRepository<ShiftHourModificationConfig,Long>, JpaSpecificationExecutor<ShiftHourModificationConfig> {
    Optional<ShiftHourModificationConfig> findByStoreId(Long storeId);
}
