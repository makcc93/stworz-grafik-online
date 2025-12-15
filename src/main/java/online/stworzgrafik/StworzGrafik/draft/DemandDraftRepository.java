package online.stworzgrafik.StworzGrafik.draft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface DemandDraftRepository extends JpaRepository<DemandDraft,Long>, JpaSpecificationExecutor<DemandDraft> {
    boolean existsByStoreIdAndDate(Long storeId, LocalDate draftDate);
    Optional<DemandDraft> findByStoreIdAndDate(Long storeId, LocalDate draftDate);
    List<DemandDraft> findByStoreIdAndDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);
}
