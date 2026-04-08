package online.stworzgrafik.StworzGrafik.draft;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface DemandDraftRepository extends JpaRepository<DemandDraft,Long>, JpaSpecificationExecutor<DemandDraft> {
    boolean existsByStoreIdAndDraftDate(Long storeId, LocalDate draftDate);
    Page<DemandDraft> findByStore_IdAndDraftDateBetween(Long storeId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    Optional<DemandDraft> findByStore_IdAndDraftDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);
    List<DemandDraft> findAllByStore_IdAndDraftDateBetween(Long storeId, LocalDate startDay, LocalDate endDay);
}
