package online.stworzgrafik.StworzGrafik.draft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface DemandDraftRepository extends JpaRepository<DemandDraft,Long>, JpaSpecificationExecutor<DemandDraft> {
    boolean existsByStoreIdAndDraftDate(Long storeId, LocalDate draftDate);
    Optional<DemandDraft> findByStoreIdAndDraftDate(Long storeId, LocalDate draftDate);
    List<DemandDraft> findByStoreIdAndDraftDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);
}
